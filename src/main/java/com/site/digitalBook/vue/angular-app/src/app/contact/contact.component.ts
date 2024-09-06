import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../services/auth/auth.service';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FooterComponent } from "../footer/footer.component";
import { NavbarComponent } from "../navbar/navbar.component";

@Component({
  selector: 'app-contact',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FooterComponent, NavbarComponent],
  templateUrl: './contact.component.html',
  styleUrls: ['./contact.component.css']
})
export class ContactComponent implements OnInit {
  contactForm!: FormGroup;
  successMessage: string | null = null;
  errorMessage: string | null = null;

  constructor(private fb: FormBuilder, private authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.contactForm = this.fb.group({
      subject: ['', Validators.required],
      message: ['', Validators.required]
    });
  }

  onSubmit(): void {
    if (this.contactForm.valid) {
      const { subject, message } = this.contactForm.value;
      const email = localStorage.getItem('userEmail') || '';

      this.authService.sendContactMessage(email, subject, message).subscribe(
        response => {
          this.successMessage = 'Votre message a été envoyé et est en attente de traitement.';
          setTimeout(() => {
            this.successMessage = null;
          }, 3000);
          this.contactForm.reset();
        },
        error => {
          this.errorMessage = 'Une erreur est survenue lors de l\'envoi de votre message. Veuillez réessayer.';
          setTimeout(() => {
            this.errorMessage = null;
          }, 3000);
        }
      );
    }
  }
}
