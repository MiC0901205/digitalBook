import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';

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
  actionType: string = 'confirmation'; // 'login' ou 'confirmation'
  isSubmitted: boolean = false;
  successMessage: string | null = null;
  errorMessage: string | null = null;
  storedEmail: string | null = null;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.actionForm = this.fb.group({
      code: ['', [Validators.required]]
    });
  }

  ngOnInit(): void {
    // Vérifiez ce qui est stocké dans localStorage
    const storedEmail = localStorage.getItem('userEmail');
    console.log('Contenu de localStorage pour userEmail:', storedEmail); // Devrait afficher l'email stocké
  
    // Initialisez storedEmail dans le composant
    this.storedEmail = storedEmail;
    
    // Ajoutez le champ email dans le formulaire si l'action est 'confirmation'
    if (this.actionType === 'confirmation') {
      this.actionForm.addControl('email', this.fb.control(this.storedEmail, [Validators.required, Validators.email]));
    }
  }   

  onSubmit(): void {
    this.isSubmitted = true;
    if (this.actionForm.valid) {
      const { code, email } = this.actionForm.value;
      const emailToSend = email || this.storedEmail;
  
      if (emailToSend) {
        this.authService.verifyCode(emailToSend, code).subscribe(
          response => {
            console.log('Code verification successful!', response);
            this.successMessage = 'Code vérifié avec succès !';
            this.errorMessage = null;
  
            const token = response.token;
            if (token) {
              localStorage.setItem('userToken', token);
              this.authService.setEmail(emailToSend);
              this.authService.setLoggedInStatus(true);
            }
  
            this.router.navigate(['/']);
          },
          error => {
            console.error('Code verification failed', error);
            this.successMessage = null;
            this.errorMessage = error.error?.message ? error.error.message : 'Code invalide ou expiré.';
          }
        );
      } else {
        this.errorMessage = 'Email non trouvé.';
      }
    } else {
      this.actionForm.markAllAsTouched();
    }
  }   
}
