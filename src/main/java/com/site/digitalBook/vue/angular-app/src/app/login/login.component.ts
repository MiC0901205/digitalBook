import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common'; // Assurez-vous d'importer CommonModule
import { RecaptchaModule } from 'ng-recaptcha';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
  standalone: true,
  imports: [
    CommonModule, 
    ReactiveFormsModule,
    RecaptchaModule,
  ],
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  isSubmitted = false;
  recaptchaToken: string = '';

  constructor(private fb: FormBuilder) {}

  ngOnInit(): void {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
  }

  handleCaptchaResponse(response: string | null): void {
    this.recaptchaToken = response || '';
  }

  onSubmit(): void {
    this.isSubmitted = true;
    if (this.loginForm.valid && this.recaptchaToken) {
      console.log('Form Submitted!', this.loginForm.value);
    } else {
      console.log('Form is not valid or CAPTCHA not completed. Please fill in all required fields and complete the CAPTCHA.');
      this.loginForm.markAllAsTouched();
    }
  }
}
