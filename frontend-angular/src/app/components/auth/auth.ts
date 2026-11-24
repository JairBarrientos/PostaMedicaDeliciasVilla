import { Component, OnInit, signal, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { PacienteService } from '../../services/paciente.service';
import { UsuarioService } from '../../services/usuario.service';
import { InactividadService } from '../../services/inactividad.service';
import { Paciente } from '../../model/paciente';

interface ErroresCampo {
  nombres?: string;
  apellidos?: string;
  dni?: string;
  correo?: string;
  telefono?: string;
  password?: string;
  sis?: string;
}

@Component({
  selector: 'app-auth',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './auth.html',
  styleUrl: './auth.css'
})
export class AuthComponent implements OnInit {

  private readonly pacienteService = inject(PacienteService);
  private readonly usuarioService  = inject(UsuarioService);
  private readonly inactividad     = inject(InactividadService);
  private readonly router          = inject(Router);
  private readonly route           = inject(ActivatedRoute);

  vista = signal<'login' | 'registro'>('login');

  loginData = { identificador: '', password: '', rol: 'PACIENTE' };

  registroData: Paciente = {
    nombres: '', apellidos: '', dni: '', correo: '',
    password: '', telefono: '', afiliadoSis: false
  };

  errores: ErroresCampo = {};
  mostrarTelefono = false;

  mostrarModalExito = signal(false);
  cargandoRegistro = signal(false);

  mostrarModalBienvenida = signal(false);
  mensajeBienvenida = signal('');
  iconoBienvenida = signal('fa-solid fa-user-check');

  mostrarModalAlerta = signal(false);
  alertaTitulo = signal('');
  alertaDescripcion = signal('');
  alertaTipo = signal<'error' | 'warning'>('error');

  notificacion = signal<{ mensaje: string; tipo: 'exito' | 'error' | 'warning' } | null>(null);

  ngOnInit(): void {
    this.inactividad.detener();

    this.route.queryParams.subscribe(params => {
      if (params['sesionExpirada'] === 'true') {
        this.mostrarAlerta(
          'Sesión expirada',
          'Tu sesión se cerró automáticamente por 5 minutos de inactividad. Por favor, inicia sesión de nuevo.',
          'warning'
        );
        this.router.navigate([], { queryParams: {}, replaceUrl: true });
      }
    });
  }

  onLogin() {
    if (!this.loginData.identificador || !this.loginData.password) {
      this.mostrarAlerta('Campos vacíos', 'Ingresa tu DNI o correo y tu contraseña para continuar.', 'warning');
      return;
    }

    this.usuarioService.login({
      identificador: this.loginData.identificador.trim(),
      password: this.loginData.password,
      rol: this.loginData.rol
    }).subscribe({
      next: (resp) => {
        const rol = resp.rol?.toUpperCase();
        const rolSeleccionado = this.loginData.rol?.toUpperCase();

        if (rol !== rolSeleccionado) {
          this.mostrarAlerta('Rol incorrecto', 'Las credenciales no corresponden al rol seleccionado. Verifica e intenta de nuevo.', 'error');
          return;
        }

        if (rol === 'ADMIN') {
          localStorage.setItem('admin_logueado', JSON.stringify(resp));
          this.inactividad.iniciar();
          this.mostrarBienvenida('Bienvenido', 'Administrador', 'fa-solid fa-shield-halved', () => this.router.navigate(['/admin']));
        } else if (rol === 'ESPECIALISTA') {
          localStorage.setItem('especialista_logueado', JSON.stringify(resp));
          this.inactividad.iniciar();
          this.mostrarBienvenida('Bienvenido', `Dr. ${resp.nombreCompleto}`, 'fa-solid fa-user-doctor', () => this.router.navigate(['/especialista']));
        } else if (rol === 'PACIENTE') {
          localStorage.setItem('paciente_logueado', JSON.stringify(resp));
          this.inactividad.iniciar();
          this.mostrarBienvenida('Bienvenido de nuevo', resp.nombreCompleto, 'fa-solid fa-heart-pulse', () => this.router.navigate(['/dashboard']));
        } else {
          this.mostrarAlerta('Error', 'Rol no reconocido. Contacta al administrador.', 'error');
        }
      },
      error: (err) => {
        const msg: string = err.error || '';
        if (msg.toLowerCase().includes('pendiente') || msg.toLowerCase().includes('aprobac')) {
          this.mostrarAlerta('Cuenta pendiente', 'Tu cuenta aún no ha sido aprobada por el administrador. Por favor, espera la confirmación.', 'warning');
        } else if (msg.startsWith('BLOQUEADO:')) {
          const dias = msg.split(':')[1];
          this.mostrarAlerta('Registro bloqueado', `Has agotado los 3 intentos de registro. Debes esperar ${dias} día(s) más para poder intentarlo de nuevo.`, 'error');
        } else if (msg.startsWith('RECHAZADO:')) {
          const restantes = msg.split(':')[1];
          const restantesNum = parseInt(restantes);
          if (restantesNum === 0) {
            this.mostrarAlerta('Cuenta rechazada — sin intentos', 'Tu solicitud fue rechazada por tercera vez. Debes esperar 1 mes para volver a intentar el registro.', 'error');
          } else {
            this.mostrarAlerta('Cuenta rechazada', `Tu solicitud fue rechazada porque no se pudo verificar tu afiliación al SIS. Te quedan ${restantes} intento(s) de registro. Cuando cuentes con el SIS activo, regístrate de nuevo con tu DNI.`, 'error');
          }
        } else {
          this.mostrarAlerta('Credenciales incorrectas', 'El DNI, correo o contraseña que ingresaste no son correctos. Verifica e intenta de nuevo.', 'error');
        }
      }
    });
  }

  onRegister() {
    this.errores = {};
    if (!this.validarRegistro()) return;

    this.cargandoRegistro.set(true);
    this.mostrarModalExito.set(true);

    this.pacienteService.registrar(this.registroData).subscribe({
      next: () => {
        setTimeout(() => { this.cargandoRegistro.set(false); }, 2000);
        setTimeout(() => {
          this.mostrarModalExito.set(false);
          this.vista.set('login');
          this.limpiarRegistro();
        }, 5000);
      },
      error: (err) => {
        this.mostrarModalExito.set(false);
        this.cargandoRegistro.set(false);
        const msg: string = typeof err.error === 'string' ? err.error : (err.error?.message || err.message || '');
        if (msg.startsWith('BLOQUEADO:')) {
          const dias = msg.split(':')[1];
          this.mostrarAlerta(
            'Registro bloqueado',
            `No puedes registrarte en este momento. Has agotado tus 3 intentos y debes esperar ${dias} día(s) más antes de poder volver a intentarlo. Esto ocurre por motivos de verificación del SIS.`,
            'error'
          );
        } else if (msg.toLowerCase().includes('telefono') || msg.toLowerCase().includes('teléfono')) {
          this.errores.telefono = 'Este número de teléfono ya está registrado';
        } else if (msg.toLowerCase().includes('correo') || msg.toLowerCase().includes('email')) {
          this.errores.correo = 'Este correo ya está registrado';
        } else if (msg.toLowerCase().includes('dni')) {
          this.errores.dni = 'Este DNI ya está registrado';
        } else {
          this.mostrarNotificacion(msg || 'Error al registrar', 'error');
        }
      }
    });
  }

  limpiarError(campo: keyof ErroresCampo) {
    if (this.errores[campo]) this.errores[campo] = undefined;
  }

  private validarRegistro(): boolean {
    const { nombres, apellidos, dni, correo, password, telefono } = this.registroData;
    let valido = true;

    if (!/^[a-zA-ZÀ-ÿ\s]{3,25}$/.test(nombres))            { this.errores.nombres   = 'Mínimo 3 letras, máximo 25, solo letras'; valido = false; }
    if (!/^[a-zA-ZÀ-ÿ\s]{3,25}$/.test(apellidos))          { this.errores.apellidos = 'Mínimo 3 letras, máximo 25, solo letras'; valido = false; }
    if (!/^\d{8}$/.test(dni))                               { this.errores.dni       = 'El DNI debe tener exactamente 8 dígitos'; valido = false; }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(correo))        { this.errores.correo    = 'Ingresa un correo electrónico válido';    valido = false; }
    if (!/^9\d{8}$/.test(telefono))                         { this.errores.telefono  = 'Debe tener 9 dígitos y empezar con 9';   valido = false; }
    if (!password || password.length < 6)                   { this.errores.password  = 'La contraseña debe tener mínimo 6 caracteres'; valido = false; }
    else if (!/[a-zA-Z]/.test(password))                   { this.errores.password  = 'Debe contener al menos una letra';       valido = false; }
    else if (!/[0-9]/.test(password))                      { this.errores.password  = 'Debe contener al menos un número';      valido = false; }
    else if (!/[^a-zA-Z0-9]/.test(password))              { this.errores.password  = 'Debe contener al menos un carácter especial (!@#$...)'; valido = false; }
    if (!this.registroData.afiliadoSis)                     { this.errores.sis       = 'Debes estar afiliado al SIS para registrarte'; valido = false; }

    return valido;
  }

  cerrarModalAlerta() { this.mostrarModalAlerta.set(false); }

  private mostrarAlerta(titulo: string, descripcion: string, tipo: 'error' | 'warning') {
    this.alertaTitulo.set(titulo);
    this.alertaDescripcion.set(descripcion);
    this.alertaTipo.set(tipo);
    this.mostrarModalAlerta.set(true);
  }

  private mostrarBienvenida(saludo: string, nombre: string, icono: string, callback: () => void) {
    this.mensajeBienvenida.set(`${saludo}, ${nombre}`);
    this.iconoBienvenida.set(icono);
    this.mostrarModalBienvenida.set(true);
    setTimeout(() => { this.mostrarModalBienvenida.set(false); callback(); }, 2500);
  }

  private mostrarNotificacion(mensaje: string, tipo: 'exito' | 'error' | 'warning') {
    this.notificacion.set({ mensaje, tipo });
    setTimeout(() => this.notificacion.set(null), 3500);
  }

  private limpiarRegistro() {
    this.registroData = { nombres: '', apellidos: '', dni: '', correo: '', password: '', telefono: '', afiliadoSis: false };
    this.errores = {};
  }
}