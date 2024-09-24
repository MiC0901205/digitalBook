import { Component, OnInit } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../services/auth/auth.service';
import { CartService } from '../services/cart/cart.service';
import { CommonModule, NgIf, NgClass } from '@angular/common';
import { Observable } from 'rxjs';
import { ChangeDetectorRef } from '@angular/core'; // Ajoutez ChangeDetectorRef

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
  userProfile: string | null = null;
  userName: string | null = null;
  userId: number | null = null; // Variable pour stocker l'ID de l'utilisateur
  cartItemCount$: Observable<number>;

  constructor(
    private authService: AuthService,
    private cartService: CartService,
    private router: Router,
    private cdr: ChangeDetectorRef // Injecter ChangeDetectorRef
  ) 
  {
    // Initialisez cartItemCount$ ici pour éviter le problème de nullité
    this.cartItemCount$ = this.cartService.getCartItemCountObservable();
  }

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
          this.userId = user.data.id; // Stocker l'ID de l'utilisateur
          
          // Appeler la méthode pour mettre à jour le nombre d'éléments du panier
          this.cartService.updateCartItemCount(this.userId);
        });
      }
    });
  }
  
  loadUserProfile(): void {
    this.userProfile = this.authService.getUserProfile();
    console.log('loadUserProfile - userProfile:', this.userProfile);
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
