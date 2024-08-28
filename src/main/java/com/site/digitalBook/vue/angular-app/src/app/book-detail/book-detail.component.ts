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
  userId?: number; // Utilisez undefined pour indiquer que la valeur n'est pas encore définie
  isInCart: boolean = false; // État pour vérifier si le livre est dans le panier
  cartItems: Book[] = []; // Assurez-vous de définir cartItems comme un tableau de Book

  constructor(
    private route: ActivatedRoute,
    private bookService: BookService,
    private cartService: CartService,
    private router: Router,
    private authService: AuthService,
    private cookieService: CookieService // Ajouter CookieService ici
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = Number(params.get('id'));
      console.log('ID du livre récupéré de la route:', id);

      this.bookService.getBookById(id).subscribe({
        next: (response: Book) => {
          this.book = response;
          console.log('Détails du livre récupérés:', this.book);

          this.formattedCategories = this.formatCategories(response.categories || []);
          console.log('Catégories formatées:', this.formattedCategories);

          if (this.book.categories && this.book.categories.length > 0) {
            this.loadRecommendedBooks(this.book.categories[0].name);
          }

          // Assurez-vous que userId a bien été récupéré avant d'appeler cette méthode
          this.checkIfBookIsInCart();
        },
        error: (err: any) => console.error('Erreur lors de la récupération des détails du livre', err)
      });
    });

    this.authService.getUserId().subscribe({
      next: (id: number) => {
        this.userId = id;
        console.log('ID Utilisateur récupéré:', this.userId);
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
      // Si l'utilisateur est connecté
      if (book.id && !this.isInCart) {
        console.log('Ajout au panier pour utilisateur connecté:', { userId: this.userId, bookId: book.id });

        this.cartService.addToCart(this.userId, book.id).subscribe({
          next: () => {
            if (this.userId !== undefined) {
              this.cartService.updateCartItemCount(this.userId);
            }
            this.isInCart = true;
          },
          error: (err: any) => console.error('Erreur lors de l\'ajout du livre au panier', err)
        });
      } else {
        console.log('Le livre est déjà dans le panier ou les données sont manquantes.');
      }
    } else {
      // Si l'utilisateur n'est pas connecté, utiliser les cookies
      this.addToCartInCookies(book);
    }
  }

  private addToCartInCookies(book: Book): void {
    const cartCookie = this.cookieService.get('cart') || '[]';
    const cart: Book[] = JSON.parse(cartCookie);

    // Vérifiez si le livre est déjà dans le panier
    if (!cart.some(item => item.id === book.id)) {
      cart.push(book);
      this.cookieService.set('cart', JSON.stringify(cart));
      console.log(`Livre ${book.titre} ajouté au panier dans les cookies.`);
    } else {
      console.log('Le livre est déjà dans le panier (cookies).');
    }
  }

  private loadRecommendedBooks(category: string): void {
    console.log('Chargement des livres recommandés pour la catégorie:', category);

    this.bookService.getBooksByCategory(category).subscribe({
      next: (books: Book[]) => {
        console.log('Livres recommandés reçus:', books);
        this.recommendedBooks = books.filter(b => b.id !== this.book?.id);
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
    const formatted = categories.map(categorie => categorie.name.trim()).join(' ,  ');
    return formatted;
  }

  private checkIfBookIsInCart(): void {
    console.log('Checking', this.userId, this.book?.id);

    if (this.userId !== undefined && this.book?.id !== undefined) {
      console.log('Vérification du panier pour l\'ID utilisateur:', this.userId);

      this.cartService.getCartItems(this.userId).subscribe({
        next: (response: any) => {
          console.log('Réponse reçue pour les éléments du panier:', response);

          const cartItems: Book[] = response.data || [];

          this.isInCart = cartItems.some((item: Book) => item.id === this.book?.id);
          console.log('Le livre est-il dans le panier ?', this.isInCart);
        },
        error: (err: any) => console.error('Erreur lors de la récupération des éléments du panier', err)
      });
    } else {
      console.log('userId ou book.id est manquant.');
    }
  }
}
