import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl } from '@angular/forms';
import { AuthService } from '../services/auth/auth.service';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { NavbarComponent } from '../navbar/navbar.component';
import { User } from '../interface/user.model';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css'],
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    NavbarComponent
  ]
})
export class ProfileComponent implements OnInit {
  profileForm!: FormGroup;
  isSubmitted = false;
  maxLength: number = 13;
  errorMessage: string | null = null;
  successMessage: string | null = null;
  initialProfileValues: Partial<User> = {}; // Utiliser Partial pour faciliter la comparaison
  isFormChanged = false;
  initialEmail: string | null = null; // Pour stocker l'email initial

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
    this.profileForm = this.fb.group({
      id: [0],
      nom: [''],
      prenom: [''],
      email: ['', [Validators.required, Validators.email]],
      tel: [''],
      dateNaissance: [undefined, [Validators.required, this.dateValidator]],
      questionSecrete: ['', Validators.required],
      reponseSecrete: ['']
    });

    this.loadUserProfile();
    this.updatePhoneValidators();

    this.profileForm.valueChanges.subscribe(() => {
      this.updateButtonState();
    });
  }

  private loadUserProfile(): void {
    this.authService.getCurrentUser().subscribe(
      response => {
        const user = response.data;
        if (user) {
          this.initialEmail = user.email;  // Stocker l'email initial ici
          this.profileForm.patchValue({
            id: user.id,
            nom: user.nom || '',
            prenom: user.prenom || '',
            email: user.email || '',
            tel: user.tel || '',
            dateNaissance: user.dateNaissance || '',
            questionSecrete: user.questionSecrete || '',
            reponseSecrete: user.reponseSecrete || ''
          });
        } else {
          console.error('Aucun utilisateur trouvé dans la réponse.');
          this.errorMessage = 'Aucun utilisateur trouvé dans la réponse.';
        }
      },
      error => {
        console.error('Erreur lors de la récupération du profil utilisateur:', error);
        this.errorMessage = 'Erreur lors de la récupération du profil utilisateur. Veuillez réessayer plus tard.';
      }
    );
  }

  private updatePhoneValidators(): void {
    const telControl = this.profileForm.get('tel');
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
      telControl.updateValueAndValidity();
    }
  }

  private dateValidator(control: AbstractControl): { [key: string]: boolean } | null {
    const today = new Date();
    const inputDate = new Date(control.value);
    return inputDate <= today ? null : { dateInFuture: true };
  }

  toggleDropdown(): void {
    this.dropdownOpen = !this.dropdownOpen;
  }

  selectCountry(code: any): void {
    this.selectedCountry = code;
    this.profileForm.get('tel')?.setValue('');
    this.updatePhoneValidators();
    this.dropdownOpen = false;
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
    this.profileForm.get('tel')?.setValue(formattedValue);
    this.updateButtonState();
  }

  onSubmit(): void {
    this.isSubmitted = true;
    this.errorMessage = null;
    this.successMessage = null;

    if (this.profileForm.valid) {
      const user: User = this.profileForm.value;
      if (user.id) {
        this.authService.updateUserProfile(user).subscribe(
          response => {
            console.log('Profil mis à jour avec succès', response);
            this.successMessage = 'Profil mis à jour avec succès';

            // Mettre à jour l'email dans le localStorage si modifié
            if (this.initialEmail !== user.email) {
              localStorage.setItem('userEmail', user.email);
            }

            this.errorMessage = null;
            setTimeout(() => {
              this.successMessage = null;
            }, 3000); // 3 seconds
          },
          error => {
            console.error('Erreur lors de la mise à jour du profil', error);
            this.errorMessage = 'Une erreur est survenue lors de la mise à jour du profil.';
            this.successMessage = null;
            setTimeout(() => {
              this.errorMessage = null;
            }, 3000); // 3 seconds
          }
        );
      } else {
        console.error('ID de l’utilisateur non défini');
      }
    } else {
      this.profileForm.markAllAsTouched();
      console.log('Form errors:', this.profileForm.errors);
      console.log('Form controls errors:', this.profileForm.controls);
    }
  }

  private updateButtonState(): void {
    this.isFormChanged = !this.formUnchanged();
  }

  private formUnchanged(): boolean {
    const currentValues = this.profileForm.getRawValue();
    return JSON.stringify(currentValues) === JSON.stringify(this.initialProfileValues);
  }

  isSubmitButtonDisabled(): boolean {
    return !this.isFormChanged || !this.profileForm.valid;
  }
}
