import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl, ValidatorFn } from '@angular/forms';
import { AuthService } from '../services/auth/auth.service';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css'],
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule
  ]
})
export class RegisterComponent implements OnInit {
  registerForm!: FormGroup;
  isSubmitted = false;
  maxLength: number = 13;
  passwordError: string | null = null;
  errorMessage: string | null = null;

  secretQuestions: string[] = [
    'Quel est le nom de votre premier animal de compagnie ?',
    'Quel est le nom de jeune fille de votre mère ?',
    'Quelle est la marque de votre première voiture ?',
    'Quel est le prénom de votre meilleur ami d’enfance ?'
  ];

  countryCodes = [
    { value: '+33', label: 'France', flag: '../assets/flags/fr.jpeg' },
    { value: '+1', label: 'États-Unis', flag: '../assets/flags/usa.jpg' }
  ];

  selectedCountry = this.countryCodes[0];
  dropdownOpen = false;

  constructor(private fb: FormBuilder, private authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.registerForm = this.fb.group({
      nom: ['', Validators.required],
      prenom: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      mdp: ['', [Validators.required, this.passwordStrengthValidator()]], 
      tel: ['', Validators.required],
      dateNaissance: ['', [Validators.required, this.dateInPastValidator()]],
      questionSecrete: ['', Validators.required],
      reponseSecrete: ['', Validators.required]
    });

    // Update phone validators initially
    this.updatePhoneValidators();
  }

  private passwordStrengthValidator(): ValidatorFn {
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

  private dateInPastValidator(): ValidatorFn {
    return (control: AbstractControl): { [key: string]: any } | null => {
      const value = control.value;
      const today = new Date();
      if (value) {
        const selectedDate = new Date(value);
        if (selectedDate >= today) {
          return { 'dateInFuture': true };
        }
      }
      return null;
    };
  }

  toggleDropdown(): void {
    this.dropdownOpen = !this.dropdownOpen;
  }

  selectCountry(code: any): void {
    this.selectedCountry = code;
    this.registerForm.get('tel')?.setValue(''); // Clear phone input on country change
    this.updatePhoneValidators();
    this.dropdownOpen = false; // Close the dropdown after selection
  }

  updatePhoneValidators(): void {
    const telControl = this.registerForm.get('tel');
    if (telControl) {
      const validators = [Validators.required];
      
      if (this.selectedCountry.value === '+33') {
        this.maxLength = 13; 
        validators.push(
          Validators.maxLength(this.maxLength),
          Validators.pattern(/^\d{1} \d{2} \d{2} \d{2} \d{2}$/)
        );
      } else if (this.selectedCountry.value === '+1') {
        this.maxLength = 12;
        validators.push(
          Validators.maxLength(this.maxLength),
          Validators.pattern(/^\d{3}-\d{3}-\d{4}$/)
        );
      }
  
      telControl.setValidators(validators);

      console.log("validators", validators);
      telControl.updateValueAndValidity();
    }
  }

  formatPhoneInput(event: Event): void {
    const input = event.target as HTMLInputElement;
    let formattedValue = input.value.replace(/\D/g, '');

    if (this.selectedCountry.value === '+33') {
      formattedValue = formattedValue.replace(/^0/, '');
      formattedValue = formattedValue.replace(/(\d)(\d{2})(\d{2})(\d{2})(\d{2})$/, '$1 $2 $3 $4 $5');
    } else if (this.selectedCountry.value === '+1') {
      formattedValue = formattedValue.replace(/(\d{3})(\d{3})(\d{4})/, '$1-$2-$3');
    }

    input.value = formattedValue;
    this.registerForm.get('tel')?.setValue(formattedValue);
  }

  onSubmit(): void {
    this.isSubmitted = true;
    this.errorMessage = null; // Réinitialiser le message d'erreur

    if (this.registerForm.valid) {
      const formData = this.registerForm.value;
      this.authService.register(formData).subscribe(
        response => {
          console.log('Registration successful!', response);
          this.registerForm.reset();
          this.errorMessage = null; 
  
          // Stocker le succès dans le localStorage
          localStorage.setItem('registrationSuccess', 'true');

          localStorage.setItem('userEmail', formData.email);
  
          this.router.navigate(['/user-action'], { queryParams: { actionType: 'confirmation' } });
        },
        error => {
          console.error('Registration failed', error);
          this.errorMessage = 'Adresse email déjà existante. Vous pouvez vous rendre sur la page de <a href="/login">connexion</a> pour vous connecter.';
        }
      );
    } else {
      this.registerForm.markAllAsTouched();
      console.log('Form errors:', this.registerForm.errors);
      console.log('Form controls errors:', this.registerForm.controls);
    }
  }
}
