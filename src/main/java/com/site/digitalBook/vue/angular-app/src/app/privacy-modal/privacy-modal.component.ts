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
      const consentGiven = localStorage.getItem('privacyConsent');
      if (!consentGiven) {
        this.showModal = true;
      }
    }
  }

  acceptPolicy() {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem('privacyConsent', 'true');
    }
    this.showModal = false;
  }

  rejectPolicy() {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem('privacyConsent', 'false');
    }
    this.showModal = false; 
  }
}
