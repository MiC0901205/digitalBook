import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HttpClientModule } from '@angular/common/http';
import { PrivacyModalComponent } from './privacy-modal/privacy-modal.component'; // Import de la modale

@Component({
  selector: 'app-root',
  template: `
    <app-privacy-modal></app-privacy-modal> <!-- Ajout de la modale -->
    <router-outlet></router-outlet>
  `,
  standalone: true,
  imports: [
    RouterOutlet,
    HttpClientModule,
    PrivacyModalComponent 
  ]
})
export class AppComponent {}
