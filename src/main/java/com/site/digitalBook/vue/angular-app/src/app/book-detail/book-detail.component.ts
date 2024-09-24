import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BookService } from '../services/book/book.service';
import { Book } from '../interface/book.model';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FooterComponent } from '../footer/footer.component';
import { NavbarComponent } from '../navbar/navbar.component';
import { CartService } from '../services/cart/cart.service';
import { AuthService } from '../services/auth/auth.service';
import { CookieService } from 'ngx-cookie-service';

@Component({
  selector: 'app-book-detail',
  templateUrl: './book-detail.component.html',
  styleUrls: ['./book-detail.component.css'],
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FooterComponent,
    NavbarComponent,
  ]
})
export class BookDetailComponent implements OnInit {
  book: Book | null = null;
  recommendedBooks: Book[] = [];
  formattedCategories: string = '';
  userId?: number;
  isInCart: boolean = false;
  cartItemCount: number = 0; // Ajoutez cette propriété

  constructor(
    private route: ActivatedRoute,
    private bookService: BookService,
    private cartService: CartService,
    private router: Router,
    private authService: AuthService,
    private cookieService: CookieService
  ) 
  {
     // Souscription au compteur d'articles
     this.cartService.getCartItemCountObservable().subscribe(count => {
      this.cartItemCount = count;
    });
  }

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = Number(params.get('id'));

      if (id) {
        this.bookService.getBookById(id).subscribe({
          next: (response: any) => {
            console.log('Détails du livre reçus:', response);
            this.book = response.data; // Ajustez en fonction de votre réponse API

            if (this.book) {
              this.formattedCategories = this.formatCategories(this.book.categories || []);
              if (this.book.categories && this.book.categories.length > 0) {
                this.loadRecommendedBooks(this.book.categories[0].name);
              }
              this.checkIfBookIsInCart();
            }
          },
          error: (err: any) => console.error('Erreur lors de la récupération des détails du livre', err)
        });
      }
    });

    this.authService.getUserId().subscribe({
      next: (id: number) => {
        console.log('ID utilisateur récupéré:', id);
        this.userId = id;
        if (this.book) {
          this.checkIfBookIsInCart();
        }
      },
      error: (err: any) => console.error('Erreur lors de la récupération de l\'ID utilisateur', err)
    });
  }

  getImageAltText(book: Book): string {
    return `${book.titre} par ${book.auteur} - Découvrez ce livre captivant`;
  }
  addToCart(book: Book): void {
    if (this.userId !== undefined) {
      if (book.id && !this.isInCart) {
        this.cartService.addToCart(this.userId, book.id).subscribe({
          next: () => {
            this.isInCart = true;
            console.log('Livre ajouté au panier:', book);
          },
          error: (err: any) => console.error('Erreur lors de l\'ajout du livre au panier', err)
        });
      }
    } else {
      this.addToCartInCookies(book);
    }
  }
  
  private addToCartInCookies(book: Book): void {
    const cartCookie = this.cookieService.get('cart') || '[]';
    const cart: Book[] = JSON.parse(cartCookie);

    if (!cart.some(item => item.id === book.id)) {
      cart.push(book);
      this.cookieService.set('cart', JSON.stringify(cart));
      console.log('Livre ajouté aux cookies:', book);
    }
  }

  private loadRecommendedBooks(category: string): void {
    this.bookService.getBooksByCategory(category).subscribe({
      next: (response: any) => {
        console.log('Réponse reçue pour les livres recommandés:', response);
        
        // Supposons que la réponse contient une propriété 'data' qui est un tableau
        const books = response.data as Book[];
  
        if (Array.isArray(books)) {
          this.recommendedBooks = books.filter(b => b.id !== this.book?.id);
        } else {
          console.error('La réponse API ne contient pas un tableau de livres.');
        }
      },
      error: (err: any) => console.error('Erreur lors de la récupération des livres recommandés', err)
    });
  }
  

  calculateDiscountedPrice(price: number, discount: number): string {
    if (discount > 0) {
      const discountedPrice = (price * (1 - discount / 100)).toFixed(2);
      return discountedPrice;
    }
    return price.toFixed(2);
  }

  private formatCategories(categories: { name: string }[]): string {
    return categories.map(categorie => categorie.name.trim()).join(' ,  ');
  }

  private checkIfBookIsInCart(): void {
    if (this.userId !== undefined && this.book?.id !== undefined) {
      this.cartService.getCartItems(this.userId).subscribe({
        next: (response: any) => {
          console.log('Éléments du panier reçus:', response);
          const cartItems: Book[] = response.data || [];
          this.isInCart = cartItems.some((item: Book) => item.id === this.book?.id);
        },
        error: (err: any) => console.error('Erreur lors de la récupération des éléments du panier', err)
      });
    }
  }
}
