import { Component, OnInit } from '@angular/core';
import { CartService } from '../services/cart/cart.service';
import { AuthService } from '../services/auth/auth.service';
import { Book } from '../interface/book.model';
import { CommonModule } from '@angular/common';
import { FooterComponent } from '../footer/footer.component';
import { NavbarComponent } from '../navbar/navbar.component';

@Component({
  selector: 'app-cart',
  templateUrl: './cart.component.html',
  styleUrls: ['./cart.component.css'],
  standalone: true,
  imports: [
    CommonModule,
    FooterComponent,
    NavbarComponent
  ]
})
export class CartComponent implements OnInit {
  cartItems: Book[] = [];
  userId?: number; 

  constructor(
    private cartService: CartService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadUserIdAndCartItems();
  }

  loadUserIdAndCartItems(): void {
    this.authService.getUserId().subscribe({
      next: (id: number) => {
        this.userId = id;
        this.loadCartItems();
      },
      error: (err: any) => console.error('Erreur lors de la récupération de l\'ID utilisateur', err)
    });
  }

  loadCartItems(): void {
    if (this.userId !== undefined) {
      this.cartService.getCartItems(this.userId).subscribe({
        next: (response: any) => {
          console.log('Réponse reçue pour les éléments du panier:', response);
          this.cartItems = response.data || []; 
        },
        error: (err: any) => console.error('Erreur lors de la récupération des éléments du panier', err)
      });
    }
  }

  removeFromCart(item: Book): void {
    if (this.userId !== undefined) { 
      this.cartService.removeFromCart(this.userId, item.id).subscribe({
        next: () => {
          this.loadCartItems();
          if (this.userId !== undefined) {
            this.cartService.updateCartItemCount(this.userId); 
          }
        },
        error: (err: any) => console.error('Erreur lors de la suppression de l\'élément du panier', err)
      });
    }
  }

  clearCart(): void {
    if (this.userId !== undefined) { 
      this.cartService.clearCart(this.userId).subscribe({
        next: () => {
          this.loadCartItems();
          if (this.userId !== undefined) {
            this.cartService.updateCartItemCount(this.userId); 
          }
        },
        error: (err: any) => console.error('Erreur lors du vidage du panier', err)
      });
    }
  }

  validateCart(): void {
    console.log('Panier validé !');
  }
}
