import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { HttpClientModule } from '@angular/common/http';

@Component({
  selector: 'app-root',
  template: '<router-outlet></router-outlet>',
  standalone: true,
  imports: [
    RouterOutlet,
    HttpClientModule // Assurez-vous que HttpClientModule est importé ici
  ]
})
export class AppComponent {}
