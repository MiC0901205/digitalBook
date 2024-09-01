import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../services/auth/auth.service';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { RecaptchaModule } from 'ng-recaptcha';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
  standalone: true,
  imports: [
    ReactiveFormsModule, 
    CommonModule,
    RecaptchaModule
  ],
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  forgotPasswordForm!: FormGroup;
  isSubmitted = false;
  recaptchaToken: string = '';
  registrationSuccess: boolean = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;
  isError: boolean = false;
  isForgotPasswordModalOpen: boolean = false;
  isForgotPasswordSubmitted: boolean = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      mdp: ['', Validators.required]
    });
  
    this.forgotPasswordForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  
    this.registrationSuccess = localStorage.getItem('registrationSuccess') === 'true';
    if (this.registrationSuccess) {
      setTimeout(() => {
        this.registrationSuccess = false;
      }, 3000);
    }
    localStorage.removeItem('registrationSuccess');
  
    // Récupération du message de succès pour la réinitialisation du mot de passe
    this.successMessage = localStorage.getItem('passwordResetSuccessMessage');
    if (this.successMessage) {
      setTimeout(() => {
        this.successMessage = null;
      }, 3000);
      localStorage.removeItem('passwordResetSuccessMessage');
    }
  }

  handleCaptchaResponse(response: string | null): void {
    this.recaptchaToken = response || '';
  }

  onSubmit(): void {
    this.isSubmitted = true;
    if (this.loginForm.valid) {
        const { email, mdp } = this.loginForm.value;
        this.authService.login(email, mdp).subscribe(
            (response: any) => {
                this.errorMessage = null;
                this.successMessage = 'Connexion réussie !';
                this.isError = false;
                localStorage.setItem('userEmail', email);
                this.router.navigate(['/']);
            },
            (error: HttpErrorResponse) => {
                console.error('Login failed', error);
                this.successMessage = null;
                this.errorMessage = error.error?.message || 'Adresse email ou mot de passe incorrect';
                this.isError = true;
            }
        );
    } else {
        this.loginForm.markAllAsTouched();
    }
  }

  openForgotPasswordModal(): void {
    this.isForgotPasswordModalOpen = true;
  }
  
  closeForgotPasswordModal(): void {
    this.isForgotPasswordModalOpen = false;
  }
  
  submitForgotPassword(): void {
    this.isForgotPasswordSubmitted = true;
    if (this.forgotPasswordForm.valid) {
      const email = this.forgotPasswordForm.value.email;
      this.authService.forgotPassword(email).subscribe(
        response => {
          this.successMessage = 'Un lien pour réinitialiser votre mot de passe a été envoyé à votre adresse email.';
          this.errorMessage = null;
          this.isError = false;
        },
        (error: HttpErrorResponse) => {
          console.error('Password reset failed', error);
          this.successMessage = null;
          this.errorMessage = error.error?.message || 'Une erreur s\'est produite. Veuillez réessayer.';
          this.isError = true;
        }
      );
      this.closeForgotPasswordModal();
    } else {
      this.forgotPasswordForm.markAllAsTouched();
    }
  }
}
