import { Component, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../services/auth/auth.service';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { isPlatformBrowser, CommonModule } from '@angular/common';
import { RecaptchaModule } from 'ng-recaptcha';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RecaptchaModule
  ]
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  secretQuestionForm!: FormGroup;
  isSubmitted = false;
  isSubmittedSecretQuestion = false;
  recaptchaToken: string = '';
  registrationSuccess: boolean = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;
  isError: boolean = false;
  isForgotPasswordModalOpen: boolean = false;
  isSecretQuestionModalOpen: boolean = false;
  securityQuestion: string | null = null;

  private isBrowser: boolean;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    this.isBrowser = isPlatformBrowser(this.platformId);
  }

  ngOnInit(): void {
    this.initializeForms();
    this.handleLocalStorage();
    this.checkUserLoginStatus();
  }

  initializeForms(): void {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      mdp: ['', Validators.required]
    });

    this.secretQuestionForm = this.fb.group({
      securityAnswer: ['', Validators.required] 
    });
  }

  handleLocalStorage(): void {
    if (this.isBrowser) {
      this.registrationSuccess = localStorage.getItem('registrationSuccess') === 'true';
      if (this.registrationSuccess) {
        setTimeout(() => this.registrationSuccess = false, 3000);
      }
      localStorage.removeItem('registrationSuccess');

      this.successMessage = localStorage.getItem('passwordResetSuccessMessage');
      if (this.successMessage) {
        setTimeout(() => this.successMessage = null, 3000);
        localStorage.removeItem('passwordResetSuccessMessage');
      }
    }
  }

  checkUserLoginStatus(): void {
    this.authService.isLoggedIn().subscribe(isLoggedIn => {
      if (isLoggedIn) {
        this.router.navigate(['/']);
      }
    });
  }

  handleCaptchaResponse(response: string | null): void {
    this.recaptchaToken = response || '';
  }

  onSubmit(): void {
    this.isSubmitted = true;

    if (this.loginForm.valid && this.recaptchaToken) {
      const { email, mdp } = this.loginForm.value;

      this.authService.login(email, mdp).subscribe(
        (response: any) => {
          this.successMessage = 'Connexion réussie !';
          this.errorMessage = null;
          this.isError = false;

          if (this.isBrowser) {
            localStorage.setItem('userEmail', email);
            localStorage.setItem('userToken', response.token); // Stockez le token ici
          }

          this.authService.setLoggedInStatus(true); // Mettez à jour l'état de connexion
          this.router.navigate(['/']);
        },
        (error: HttpErrorResponse) => {
          this.successMessage = null;
          this.errorMessage = error.error?.message || 'Adresse email ou mot de passe incorrect';
          this.isError = true;
        }
      );
    } else {
      this.loginForm.markAllAsTouched();
    }
  }

  openSecretQuestionModal(): void {
    this.isSecretQuestionModalOpen = true;

    if (this.isBrowser) {
      this.authService.getCurrentUser().subscribe({
        next: (response: any) => {
          this.securityQuestion = response.data.questionSecrete;
        },
        error: (error: HttpErrorResponse) => {
          this.errorMessage = 'Erreur lors de la récupération des détails de l\'utilisateur.';
        }
      });
    }
  }

  closeSecretQuestionModal(): void {
    this.isSecretQuestionModalOpen = false;
    this.securityQuestion = null;  // Réinitialiser la question de sécurité lors de la fermeture
  }

  submitSecretQuestionAnswer(): void {
    console.log('Submitting secret question answer');
    this.isSubmittedSecretQuestion = true;
  
    if (this.secretQuestionForm.valid) {
      const { securityAnswer } = this.secretQuestionForm.value;
  
      this.authService.getCurrentUser().subscribe({
        next: (response: any) => {
          console.log('Received current user data:', response);
          const reponseSecrete = response.data.reponseSecrete;
  
          console.log('Received security answer:', securityAnswer);
          console.log('Received secret answer:', reponseSecrete);
          if (securityAnswer && reponseSecrete) {
            if (securityAnswer.trim().toLowerCase() === reponseSecrete.trim().toLowerCase()) {
              this.authService.forgotPassword(response.data.email).subscribe(
                res => {
                  console.log('Password reset link sent:', res);
                  this.successMessage = 'Un lien pour réinitialiser votre mot de passe a été envoyé à votre adresse email.';
                  this.errorMessage = null;
                  this.isError = false;
                  this.closeSecretQuestionModal();
                },
                err => {
                  console.error('Error sending reset link:', err);
                  this.successMessage = null;
                  this.errorMessage = 'Une erreur s\'est produite lors de l\'envoi du lien. Veuillez réessayer.';
                  this.isError = true;
                }
              );
            } else {
              this.closeSecretQuestionModal();

              this.errorMessage = 'La réponse à la question de sécurité est incorrecte.';
              this.successMessage = null;
              this.isError = true;
            }
          } else {
            this.closeSecretQuestionModal();

            this.errorMessage = 'La réponse secrète est manquante.';
            this.successMessage = null;
            this.isError = true;
          }
        },
        error: (error: HttpErrorResponse) => {
          console.error('Error fetching current user data:', error);
          this.errorMessage = 'Erreur lors de la récupération des détails de l\'utilisateur.';
          this.successMessage = null;
          this.isError = true;
        }
      });
    } else {
      this.secretQuestionForm.markAllAsTouched();
    }
  }
  
  
}
