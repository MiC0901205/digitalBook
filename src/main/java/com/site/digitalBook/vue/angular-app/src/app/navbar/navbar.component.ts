import { Component, OnInit } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../services/auth/auth.service';
import { CartService } from '../services/cart/cart.service';
import { CommonModule, NgIf, NgClass } from '@angular/common';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css'],
  standalone: true,
  imports: [
    CommonModule,
    NgIf,
    NgClass,
    RouterModule
  ]
})
export class NavbarComponent implements OnInit {
  isLoggedIn = false;
  showUserMenu = false;
  cartItemCount$: Observable<number> | undefined;
  userProfile: string | null = null;
  userName: string | null = null;

  constructor(
    private authService: AuthService,
    private cartService: CartService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.checkLoginStatus();
    this.loadUserProfile();
    this.cartItemCount$ = this.cartService.getCartItemCountObservable();
  }

  checkLoginStatus(): void {
    this.authService.isLoggedIn().subscribe(loggedIn => {
      this.isLoggedIn = loggedIn;
      if (loggedIn) {
        this.authService.getCurrentUser().subscribe(user => {
          this.userName = user.data.prenom; 
        });
      }
    });
  }

  loadUserProfile(): void {
    this.userProfile = this.authService.getUserProfile();
  }

  toggleUserMenu(): void {
    this.showUserMenu = !this.showUserMenu;
  }

  navigateToLogin(): void {
    this.router.navigate(['/login']);
  }

  logout(): void {
    this.authService.logout().subscribe(() => {
      this.isLoggedIn = false;
      this.userName = null;
      this.router.navigate(['/login']);
    });
  }
}
