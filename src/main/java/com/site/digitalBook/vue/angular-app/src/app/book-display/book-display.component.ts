// book-display.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../navbar/navbar.component';
import { Book } from '../interface/book.model';
import { Categorie } from '../interface/categorie.model';
import { BookService } from '../services/book/book.service';
import { CategorieService } from '../services/categorie/categorie.service';

@Component({
  selector: 'app-book-display',
  templateUrl: './book-display.component.html',
  styleUrls: ['./book-display.component.css'],
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NavbarComponent
  ]
})
export class BookDisplayComponent implements OnInit {
  books: Book[] = [];
  filteredBooks: Book[] = [];
  categories: Categorie[] = [];
  filters = {
    promotion: false,
    disponibilite: false,
    prixMax: 100
  };
  selectedCategory: number | null = null;
  searchTerm: string = '';
  categoriesByType: { [type: string]: Categorie[] } = {};
  isSidebarOpen = false; // État du panneau latéral
  isModalOpen = false; // État de la modale flottante

  constructor(
    private bookService: BookService,
    private categorieService: CategorieService
  ) {}

  ngOnInit(): void {
    this.loadBooks();
    this.loadCategories();
  }

  private loadBooks(): void {
    this.bookService.getBooks().subscribe({
      next: (response: Book[]) => {
        this.books = response || [];
        this.filteredBooks = this.books; // État initial du filtre
      },
      error: (err) => console.error('Erreur lors de la récupération des livres', err)
    });
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
      const matchesPromotion = this.filters.promotion ? book.enPromotion : true;
      const matchesDisponibilite = this.filters.disponibilite ? book.disponible : true;
      const matchesPrix = book.prix <= this.filters.prixMax;
      const matchesSearchTerm = book.titre.toLowerCase().includes(this.searchTerm.toLowerCase());

      return matchesCategory && matchesPromotion && matchesDisponibilite && matchesPrix && matchesSearchTerm;
    });
  }

  resetFilters(): void {
    // Réinitialisez les filtres à leurs valeurs par défaut
    this.filters = {
      promotion: false,
      disponibilite: false,
      prixMax: 100
    };
    this.selectedCategory = null;
    this.searchTerm = ''; // Réinitialisez le terme de recherche
    this.filterBooks(); // Réapplique les filtres avec les valeurs réinitialisées
  }
  
  getCategoryTypes(): string[] {
    return Array.from(new Set(this.categories.map(c => c.type).filter(type => type !== undefined))) as string[];
  }

  toggleSidebar(): void {
    this.isSidebarOpen = !this.isSidebarOpen;
    
    // Récupérer les éléments DOM pour le conteneur de livres et le bouton de basculement
    const bookContainer = document.querySelector('.book-container') as HTMLElement;
    
    if (this.isSidebarOpen) {
      bookContainer.style.marginLeft = '360px'; // Ajuste la marge pour tenir compte de la largeur et du décalage de la sidebar
    } else {
      bookContainer.style.marginLeft = '20px'; // Réinitialise la marge lorsque le panneau est fermé
    }
  }

  toggleModal(): void {
    this.isModalOpen = !this.isModalOpen;
  }

  applyFilters(): void {
    this.filterBooks(); // Réappliquer les filtres avec les nouvelles valeurs
  }
}
