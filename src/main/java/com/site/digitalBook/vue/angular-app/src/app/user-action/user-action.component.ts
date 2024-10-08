import { Component, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, ValidatorFn, AbstractControl } from '@angular/forms';
import { AuthService } from '../services/auth/auth.service';
import { Router, ActivatedRoute } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { isPlatformBrowser } from '@angular/common';

@Component({
  selector: 'app-user-action',
  templateUrl: './user-action.component.html',
  styleUrls: ['./user-action.component.css'],
  standalone: true,
  imports: [
    ReactiveFormsModule,
    CommonModule
  ]
})
export class UserActionComponent implements OnInit {
  actionForm: FormGroup;
  actionType: string = 'forgot-password';
  isSubmitted: boolean = false;
  successMessage: string | null = null;
  errorMessage: string | null = null;
  storedEmail: string | null = null;
  passwordError: string | null = null;

  private isBrowser: boolean;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    this.isBrowser = isPlatformBrowser(this.platformId);

    this.actionForm = this.fb.group({
      newPassword: ['', [Validators.required, this.passwordStrengthValidator()]],
      confirmPassword: ['', [Validators.required]],
      code: ['', [Validators.required]],
      email: ['']
    }, { validators: this.passwordMatchValidator });
  }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.actionType = params['actionType'] || 'forgot-password';
      if (this.isBrowser) {
        this.storedEmail = localStorage.getItem('userEmail');
      }
      if (this.actionType === 'confirmation') {
        this.actionForm.get('newPassword')?.disable();
        this.actionForm.get('confirmPassword')?.disable();
        this.actionForm.get('code')?.enable();
        this.actionForm.get('email')?.setValue(this.storedEmail);
      } else {
        this.actionForm.get('code')?.disable();
        this.actionForm.get('newPassword')?.enable();
        this.actionForm.get('confirmPassword')?.enable();
      }
    });
  }

  passwordMatchValidator(formGroup: FormGroup): { [key: string]: boolean } | null {
    const newPassword = formGroup.get('newPassword')?.value;
    const confirmPassword = formGroup.get('confirmPassword')?.value;
    if (newPassword && confirmPassword && newPassword !== confirmPassword) {
      return { mismatch: true };
    }
    return null;
  }

  passwordStrengthValidator(): ValidatorFn {
    return (control: AbstractControl): { [key: string]: any } | null => {
      const value = control.value || '';
      const minLength = value.length >= 8;
      const hasUpperCase = /[A-Z]/.test(value);
      const hasLowerCase = /[a-z]/.test(value);
      const hasNumber = /\d/.test(value);
      const hasSpecialChar = /[!@#$%^&*(),.?":{}|<>]/.test(value);

      if (minLength && hasUpperCase && hasLowerCase && hasNumber && hasSpecialChar) {
        this.passwordError = null;
        return null;
      } else {
        const errors: string[] = [];
        if (!minLength) errors.push('au moins 8 caractères');
        if (!hasUpperCase) errors.push('une majuscule');
        if (!hasLowerCase) errors.push('une minuscule');
        if (!hasNumber) errors.push('un chiffre');
        if (!hasSpecialChar) errors.push('un caractère spécial');

        this.passwordError = `Le mot de passe doit contenir : ${errors.join(', ')}.`;
        return { 'passwordStrength': true };
      }
    };
  }

  onSubmit(): void {
    this.isSubmitted = true;

    if (this.actionForm.valid) {
      const { code, newPassword, email } = this.actionForm.value;
      const emailToSend = email || this.storedEmail;

      if (this.actionType === 'forgot-password') {
        if (!emailToSend) {
          this.errorMessage = 'Email non trouvé.';
          return;
        }

        this.authService.resetPassword(emailToSend, newPassword).subscribe({
          next: response => {
            this.successMessage = response.message;
            this.errorMessage = null;

            if (this.successMessage && this.isBrowser) {
              localStorage.setItem('passwordResetSuccessMessage', this.successMessage);
            }

            this.router.navigate(['/login']);
          },
          error: (error: HttpErrorResponse) => {
            console.error('Password reset failed', error);
            this.successMessage = null;
            this.errorMessage = error.error?.message || 'Erreur lors de la réinitialisation du mot de passe';
          }
        });
      } else if (this.actionType === 'confirmation') {
        if (!emailToSend) {
          this.errorMessage = 'Email non trouvé.';
          return;
        }

        this.authService.verifyCode(emailToSend, code).subscribe({
          next: response => {
            this.successMessage = response.message;
            this.errorMessage = null;

            this.router.navigate(['/login']);
          },
          error: (error: HttpErrorResponse) => {
            console.error('Code verification failed', error);
            this.successMessage = null;
            this.errorMessage = error.error?.message || 'Code invalide ou expiré.';
          }
        });
      }
    } else {
      this.actionForm.markAllAsTouched();
    }
  }
}
