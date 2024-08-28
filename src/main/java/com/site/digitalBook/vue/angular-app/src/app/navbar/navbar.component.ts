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
  userId: number | undefined;

  constructor(
    private authService: AuthService,
    private cartService: CartService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.checkLoginStatus();
    this.authService.getUserId().subscribe({
      next: (id: number) => {
        this.userId = id;
        this.cartItemCount$ = this.cartService.getCartItemCountObservable(); // Obtenir l'Observable pour le nombre d'articles
        if (this.userId) {
          this.cartService.updateCartItemCount(this.userId); // Initialiser le Subject
        }
      },
      error: (err: any) => console.error('Erreur lors de la récupération de l\'ID utilisateur', err)
    });
  }
  

  checkLoginStatus(): void {
    this.authService.isLoggedIn().subscribe(loggedIn => {
      this.isLoggedIn = loggedIn;
    });
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
      this.router.navigate(['/login']);
    });
  }
}
