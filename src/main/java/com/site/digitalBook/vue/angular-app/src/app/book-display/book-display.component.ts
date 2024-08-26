import { Component, OnInit, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../navbar/navbar.component';
import { Book } from '../interface/book.model';
import { Categorie } from '../interface/categorie.model';
import { BookService } from '../services/book/book.service';
import { CategorieService } from '../services/categorie/categorie.service';
import { FooterComponent } from "../footer/footer.component";

@Component({
  selector: 'app-book-display',
  templateUrl: './book-display.component.html',
  styleUrls: ['./book-display.component.css'],
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NavbarComponent,
    FooterComponent
  ]
})
export class BookDisplayComponent implements OnInit {
  books: Book[] = [];
  filteredBooks: Book[] = [];
  paginatedBooks: Book[] = [];
  categories: Categorie[] = [];
  filters = {
    promotion: false,
    prixMax: 4.99 // Valeur par défaut pour le filtre de prix
  };
  selectedCategory: number | null = null;
  searchTerm: string = '';
  categoriesByType: { [type: string]: Categorie[] } = {};
  currentPage: number = 1;
  itemsPerPage: number = 12; // Nombre d'éléments par page (modifiable pour responsive)
  totalPages: number = 0;
  
  priceOptions: number[] = [1.99, 2.99, 3.99, 4.99]; // Options de prix pour le filtre
  
  isSidebarOpen: boolean = false;
  isModalOpen: boolean = false;

  constructor(
    private bookService: BookService,
    private categorieService: CategorieService
  ) {}

  ngOnInit(): void {
    this.loadBooks();
    this.loadCategories();
    this.updateItemsPerPage(); // Mise à jour du nombre d'éléments par page au chargement
  }

  private loadBooks(): void {
    this.bookService.getBooks().subscribe({
      next: (response: Book[]) => {
        this.books = response || [];
        this.filteredBooks = this.books;
        this.updatePagination();
        this.paginate();
      },
      error: (err) => console.error('Erreur lors de la récupération des livres', err)
    });
  }

  getImageAltText(book: Book): string {
    return `${book.titre} par ${book.auteur} - Découvrez ce livre captivant`;
  }

  private loadCategories(): void {
    this.categorieService.getCategories().subscribe({
      next: (response: Categorie[]) => {
        this.categories = response || [];
        this.categoriesByType = this.groupCategoriesByType(this.categories);
      },
      error: (err) => console.error('Erreur lors de la récupération des catégories', err)
    });
  }

  private groupCategoriesByType(categories: Categorie[]): { [type: string]: Categorie[] } {
    return categories.reduce((acc, categorie) => {
      if (categorie.type) {
        if (!acc[categorie.type]) {
          acc[categorie.type] = [];
        }
        acc[categorie.type].push(categorie);
      }
      return acc;
    }, {} as { [type: string]: Categorie[] });
  }

  filterBooks(): void {
    this.filteredBooks = this.books.filter(book => {
      const matchesCategory = this.selectedCategory 
        ? book.categories.some(categorie => categorie.id === this.selectedCategory)
        : true;
      const matchesPromotion = this.filters.promotion ? book.remise > 0 : true;
      const matchesPrix = book.prix <= this.filters.prixMax;
      const matchesSearchTerm = book.titre.toLowerCase().includes(this.searchTerm.toLowerCase());

      return matchesCategory && matchesPromotion && matchesPrix && matchesSearchTerm;
    });
    this.updatePagination();
    this.paginate();
  }

  resetFilters(): void {
    this.filters = {
      promotion: false,
      prixMax: 4.99
    };
    this.selectedCategory = null;
    this.searchTerm = '';
    this.filterBooks();
  }
  
  getCategoryTypes(): string[] {
    return Array.from(new Set(this.categories.map(c => c.type).filter(type => type !== undefined))) as string[];
  }

  toggleSidebar(): void {
    this.isSidebarOpen = !this.isSidebarOpen;
    const bookContainer = document.querySelector('.book-container') as HTMLElement;
    if (this.isSidebarOpen) {
      bookContainer.style.marginLeft = '360px';
    } else {
      bookContainer.style.marginLeft = '20px';
    }
  }

  toggleModal(): void {
    this.isModalOpen = !this.isModalOpen;
  }

  applyFilters(): void {
    this.filterBooks();
  }

  private updatePagination(): void {
    this.totalPages = Math.max(Math.ceil(this.filteredBooks.length / this.itemsPerPage), 1);
    this.currentPage = Math.min(this.currentPage, this.totalPages);
  }

  paginate(): void {
    const start = (this.currentPage - 1) * this.itemsPerPage;
    const end = start + this.itemsPerPage;
    this.paginatedBooks = this.filteredBooks.slice(start, end);
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      this.paginate();
    }
  }

  previousPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.paginate();
    }
  }

  @HostListener('window:resize', ['$event'])
  onResize(event: any): void {
    this.updateItemsPerPage();
    this.updatePagination();
    this.paginate();
  }

  private updateItemsPerPage(): void {
    const screenWidth = window.innerWidth;
    if (screenWidth < 768) {
      this.itemsPerPage = 4;
    } else {
      this.itemsPerPage = 12;
    }
    this.updatePagination();
    this.paginate();
  }
}
