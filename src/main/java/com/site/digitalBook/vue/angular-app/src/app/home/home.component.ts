import { Component, OnInit } from '@angular/core';
import { AuthService } from '../services/auth.service';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css'],
  standalone: true,
  imports: [
    CommonModule
  ]
})
export class HomeComponent implements OnInit {
  isLoggedIn: boolean = false;
  showUserMenu: boolean = false;

  constructor(private authService: AuthService, private router: Router) {}

  ngOnInit(): void {
    this.checkLoginStatus();
  }
  
  checkLoginStatus(): void {
    this.authService.isLoggedIn().subscribe(loggedIn => {
      this.isLoggedIn = loggedIn;
      console.log(`User is logged in: ${this.isLoggedIn}`); // Ajoutez cette ligne
    });
  }  

  toggleUserMenu(): void {
    if (!this.isLoggedIn) {
      console.log('User is not logged in, redirecting to login page.');
      this.router.navigate(['/login']);
    } else {
      this.showUserMenu = !this.showUserMenu;
      console.log('Toggling user menu, currently shown:', this.showUserMenu);
    }
  }

  logout(): void {
    this.authService.logout().subscribe(() => {
      this.isLoggedIn = false;
      console.log('User logged out.');
      this.router.navigate(['/login']);
    });
  }
}
