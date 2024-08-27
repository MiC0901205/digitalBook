import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { BookService } from '../services/book/book.service';
import { Book } from '../interface/book.model';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FooterComponent } from '../footer/footer.component';
import { NavbarComponent } from '../navbar/navbar.component';

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

  constructor(
    private route: ActivatedRoute,
    private bookService: BookService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const id = Number(params.get('id'));
      this.bookService.getBookById(id).subscribe({
        next: (response: Book) => {
          this.book = response;
          this.formattedCategories = this.formatCategories(response.categories || []);
          if (this.book.categories && this.book.categories.length > 0) {
            this.loadRecommendedBooks(this.book.categories[0].name);
          }
        },
        error: (err: any) => console.error('Erreur lors de la récupération des détails du livre', err)
      });
    });
  }

  getImageAltText(book: Book): string {
    return `${book.titre} par ${book.auteur} - Découvrez ce livre captivant`;
  }

  addToCart(book: Book): void {
    console.log(`Ajout du livre ${book.titre} au panier.`);
  }

  private loadRecommendedBooks(category: string): void {
    this.bookService.getBooksByCategory(category).subscribe({
      next: (books: Book[]) => {
        this.recommendedBooks = books.filter(b => b.id !== this.book?.id);
      },
      error: (err: any) => console.error('Erreur lors de la récupération des livres recommandés', err)
    });
  }
  
  calculateDiscountAmount(originalPrice: number, discountPercent: number): number {
    return originalPrice * (discountPercent / 100);
  }

  private formatCategories(categories: { name: string }[]): string {
    return categories.map(categorie => categorie.name.trim()).join(' ,  ');
  }
}
