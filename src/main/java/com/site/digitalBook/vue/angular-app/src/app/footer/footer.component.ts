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
      console.log('URL Segments:', urlSegments); // Debugging purpose
      this.currentSection = urlSegments.length > 1 ? urlSegments[1].path : '';
      
      // If currentSection is empty or 'about', 'privacy', 'terms', 'cookies', or 'retraction'
      // Then show the footer
      this.showFooter = this.currentSection === '' || 
                        this.currentSection === 'about' || 
                        this.currentSection === 'privacy' || 
                        this.currentSection === 'terms' || 
                        this.currentSection === 'cookies' || 
                        this.currentSection === 'retraction';
      
      console.log('Current Section:', this.currentSection);
    });
  }

  scrollToTop(): void {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
}
