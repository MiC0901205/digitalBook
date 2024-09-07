import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FooterComponent } from '../footer/footer.component';
import { NavbarComponent } from "../navbar/navbar.component";
import { BookService } from '../services/book/book.service';
import { Book } from '../interface/book.model';

@Component({
  selector: 'app-home',
  standalone: true,
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css'],
  imports: [
    CommonModule,
    FormsModule,
    FooterComponent,
    NavbarComponent
  ]
})
export class HomeComponent implements OnInit {
  searchQuery: string = '';
  featuredBooks: Book[] = [];
  
  private readonly FEATURED_BOOK_COUNT = 5; 

  constructor(
    private router: Router,
    private bookService: BookService 
  ) {}

  ngOnInit(): void {
    this.loadFeaturedBooks();
  }

  performSearch(): void {
    console.log('Performing search with query:', this.searchQuery);
    if (this.searchQuery.trim()) {
      this.router.navigate(['/books'], { queryParams: { search: this.searchQuery } });
    }
  }

  loadFeaturedBooks(): void {
    this.bookService.getBooks().subscribe({
      next: (response: Book[]) => {
        this.featuredBooks = response.slice(0, this.FEATURED_BOOK_COUNT);
      },
      error: (err) => console.error('Erreur lors de la récupération des livres', err)
    });
  }

  viewBookDetail(bookId: number): void {
    console.log('Viewing book detail:', bookId);
    this.router.navigate(['/book-detail', bookId]);
  }

  // Fonction pour obtenir le texte alternatif de l'image d'un livre
  getImageAltText(book: Book): string {
    return `Couverture du livre ${book.titre} par ${book.auteur}`;
  }
}
