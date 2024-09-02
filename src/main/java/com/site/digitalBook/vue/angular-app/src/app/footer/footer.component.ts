import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-footer',
  standalone: true,
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.css'],
  imports: [
    CommonModule,
    RouterModule
  ]
})
export class FooterComponent implements OnInit {
  currentSection: string = '';
  showFooter: boolean = true;

  constructor(private route: ActivatedRoute, private router: Router) { }

  ngOnInit(): void {
    this.route.url.subscribe(urlSegments => {
      // Si il y a plusieurs segments, utiliser le deuxième segment
      if (urlSegments.length > 1) {
        this.currentSection = urlSegments[1].path;
      } else {
        this.currentSection = urlSegments.length > 0 ? urlSegments[0].path : '';
      }
      // Vérifier si currentSection est un nombre et réinitialiser si nécessaire
      if (!isNaN(Number(this.currentSection))) {
        this.currentSection = ''; // Réinitialiser si c'est un nombre
      }

      // Déterminer si le footer doit être affiché ou non
      const sectionsToHideFooter = ['sales-conditions', 'about', 'privacy', 'terms', 'cookies', 'retraction'];
      this.showFooter = !sectionsToHideFooter.includes(this.currentSection.toLowerCase());
      console.log('showFooter:', this.showFooter);
      console.log('currentSection:', this.currentSection);
      if(this.currentSection === 'books') {
        this.currentSection = '';
      }
      console.log('currentSectionFinale:', this.currentSection);

    });
  }

  scrollToTop(): void {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
  
}