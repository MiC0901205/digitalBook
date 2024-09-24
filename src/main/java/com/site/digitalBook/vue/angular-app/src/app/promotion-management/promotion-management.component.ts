import { Component, OnInit } from '@angular/core';
import { Book } from '../interface/book.model'; 
import { BookService } from '../services/book/book.service'; 
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../navbar/navbar.component';
import { FooterComponent } from '../footer/footer.component';

@Component({
  selector: 'app-promotion-management',
  templateUrl: './promotion-management.component.html',
  styleUrls: ['./promotion-management.component.css'],
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NavbarComponent,
    FooterComponent
  ]
})
export class PromotionManagementComponent implements OnInit {
  books: Book[] = []; 
  paginatedBooks: Book[] = [];
  currentPage: number = 1;
  booksPerPage: number = 10;
  totalPages: number = 0; 

  isModalOpen = false;
  currentBook: Book | null = null;
  showSuccessMessage: boolean = false; 

  constructor(private bookService: BookService) {}

  ngOnInit(): void {
    this.loadBooks();
  }

  // Charger les livres à partir du backend
  loadBooks() {
    this.bookService.getBooks().subscribe({
      next: (response: any) => {
        
        if (response && Array.isArray(response.data)) {
          this.books = response.data; 

          // Mettez à jour le nombre total de pages et les livres paginés
          this.totalPages = Math.ceil(this.books.length / this.booksPerPage);
          this.updatePaginatedBooks();
        } else {
          console.error('La réponse de l\'API est mal formatée ou la propriété data n\'est pas un tableau', response);
          this.books = []; 
        }
      },
      error: (err) => console.error('Erreur lors du chargement des livres :', err)
    });
  }

  // Mettre à jour les livres paginés pour la page courante
  updatePaginatedBooks() {
    const start = (this.currentPage - 1) * this.booksPerPage;
    const end = start + this.booksPerPage;
    this.paginatedBooks = this.books.slice(start, end);
  }

  // Changer de page
  changePage(page: number) {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      this.updatePaginatedBooks();
    }
  }

  // Ouvrir la modale pour modifier la promotion d'un livre
  editPromotion(book: Book) {
    this.currentBook = { ...book };
    this.isModalOpen = true;
  }

  // Mettre à jour la promotion
  updatePromotion() {
    if (this.currentBook) {
      const updateData = {
        remise: this.currentBook.remise
      };
  
      this.bookService.updateBook(this.currentBook.id, updateData).subscribe({
        next: () => {
          this.loadBooks();
  
          this.showSuccessMessage = true;
  
          setTimeout(() => {
            this.showSuccessMessage = false;
          }, 3000);
  
          this.closeModal();
        },
        error: (err) => console.error('Erreur lors de la mise à jour du livre :', err)
      });
    }
  }

  // Fermer la modale
  closeModal() {
    this.isModalOpen = false;
    this.currentBook = null;
  }
}
