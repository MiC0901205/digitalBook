import { Component, OnInit, Inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-privacy-modal',
  templateUrl: './privacy-modal.component.html',
  styleUrls: ['./privacy-modal.component.css'],
  standalone: true,
  imports: [CommonModule]  // Ajouter CommonModule ici
})
export class PrivacyModalComponent implements OnInit {
  showModal: boolean = false;

  constructor(@Inject(PLATFORM_ID) private platformId: Object) {}

  ngOnInit() {
    if (isPlatformBrowser(this.platformId)) {
      // Vérifie si le consentement est déjà stocké
      const consentGiven = localStorage.getItem('privacyConsent');
      
      // Si aucune décision n'a été prise, affiche la modale
      if (!consentGiven) {
        this.showModal = true;
      }
    }
  }

  acceptPolicy() {
    if (isPlatformBrowser(this.platformId)) {
      // Stocke le consentement dans localStorage
      localStorage.setItem('privacyConsent', 'true');
    }
    this.showModal = false;  // Masque la modale après acceptation
  }

  rejectPolicy() {
    if (isPlatformBrowser(this.platformId)) {
      // Stocke le refus dans localStorage
      localStorage.setItem('privacyConsent', 'false');
    }
    this.showModal = false;  // Masque la modale après refus
  }
}
