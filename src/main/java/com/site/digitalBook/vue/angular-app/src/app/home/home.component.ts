import { Component } from '@angular/core';
import { NavbarComponent } from '../navbar/navbar.component'; 

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css'],
  standalone: true,
  imports: [
    NavbarComponent 
  ]
})
export class HomeComponent {}
