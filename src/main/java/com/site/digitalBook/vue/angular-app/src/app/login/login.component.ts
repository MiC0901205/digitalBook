import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../services/auth.service';
import { RecaptchaModule } from 'ng-recaptcha';
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';  // Importation correcte
import { HttpClientModule } from '@angular/common/http';  // Importez HttpClientModule ici
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
  standalone: true,
  imports: [
    RecaptchaModule,
    ReactiveFormsModule,
    CommonModule,
    HttpClientModule
  ]
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  isSubmitted = false;
  recaptchaToken: string = '';
  registrationSuccess: boolean = false;
  errorMessage: string = ''; 
  isError: boolean = false;   

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

    this.registrationSuccess = localStorage.getItem('registrationSuccess') === 'true';
    if (this.registrationSuccess) {
      setTimeout(() => {
        this.registrationSuccess = false;
      }, 3000);
    }
    localStorage.removeItem('registrationSuccess');
  }

  handleCaptchaResponse(response: string | null): void {
    this.recaptchaToken = response || '';
  }

  onSubmit(): void {
    this.isSubmitted = true;
    if (this.loginForm.valid && this.recaptchaToken) {
      const { email, mdp } = this.loginForm.value;
      this.authService.login(email, mdp).subscribe(
        response => {
          console.log('Login successful!', response);
          this.errorMessage = ''; 
          this.isError = false;  
  
          // Stocker l'email dans localStorage
          localStorage.setItem('userEmail', email);
  
          this.router.navigate(['/user-action'], { queryParams: { type: 'confirmation' } });
        },
        (error: HttpErrorResponse) => {
          console.error('Login failed', error);
          this.errorMessage = error.error?.message ? error.error.message : 'Adresse email ou mot de passe incorrect';
          this.isError = true;  
        }
      );
    } else {
      console.log('Form is not valid or CAPTCHA not completed. Please fill in all required fields and complete the CAPTCHA.');
      this.loginForm.markAllAsTouched();
    }
  }
}
