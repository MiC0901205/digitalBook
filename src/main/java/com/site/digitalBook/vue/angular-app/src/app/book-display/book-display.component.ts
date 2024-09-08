import { Component, OnInit, HostListener, Inject, PLATFORM_ID } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NavbarComponent } from '../navbar/navbar.component';
import { FooterComponent } from '../footer/footer.component';
import { Book } from '../interface/book.model';
import { Categorie } from '../interface/categorie.model';
import { BookService } from '../services/book/book.service';
import { CategorieService } from '../services/categorie/categorie.service';
import { isPlatformBrowser } from '@angular/common';
import { AuthService } from '../services/auth/auth.service';

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
    prixMax: 4.99
  };
  selectedCategory: number | null = null;
  searchTerm: string = '';
  categoriesByType: { [type: string]: Categorie[] } = {};
  currentPage: number = 1;
  itemsPerPage: number = 12; 
  itemsPerRow: number = 4;
  totalPages: number = 0;
  
  priceOptions: number[] = [1.99, 2.99, 3.99, 4.99];
  
  isSidebarOpen: boolean = false;
  isModalOpen: boolean = false;
  newBook: Partial<Book> = {}; 
  userProfile: string | null = null;
  selectedCategories: number[] = [];
  modalSelectedCategories: number[] = []; 
  showSuccessMessage: boolean = false;
  searchQuery: string = ''; // Déclarer 'searchQuery'

  private isBrowser: boolean;

  constructor(
    private bookService: BookService,
    private authService: AuthService,
    private categorieService: CategorieService,
    private router: Router,
    private route: ActivatedRoute, 
    @Inject(PLATFORM_ID) private platformId: Object
  ) {
    this.isBrowser = isPlatformBrowser(this.platformId);
  }

  ngOnInit(): void {
    this.loadBooks();
    this.loadUserProfile();
    this.loadCategories();
    this.updateItemsPerPage();
    // Écoute des changements dans les paramètres de l'URL
    this.route.queryParams.subscribe(params => {
      this.searchQuery = params['search'] || '';  // Récupère le terme de recherche
      console.log('Search Query:', this.searchQuery); // Déboguer la requête de recherche
      this.filterBooks();  // Applique le filtre après avoir chargé les livres
    });
  }  

   // Ajouter la méthode getBooks si elle n'existe pas encore
   getBooks(): void {
    // Logique pour charger les livres filtrés par la recherche
    this.filteredBooks = this.books.filter(book => 
      book.titre.toLowerCase().includes(this.searchQuery.toLowerCase())
    );
    this.paginate();
  }

  toggleCategory(categoryId: number): void {
    const index = this.selectedCategories.indexOf(categoryId);
    if (index === -1) {
      this.selectedCategories.push(categoryId);
    } else {
      this.selectedCategories.splice(index, 1);
    }
  }

  isCategorySelected(categoryId: number): boolean {
    return this.selectedCategories.includes(categoryId);
  }

  // Méthode pour appliquer les filtres
  applyFilters(): void {
    this.filterBooks();
  }

  loadUserProfile(): void {
    this.userProfile = this.authService.getUserProfile();
  }

  // Méthode pour fermer la modale
  closeModal(): void {
    this.toggleModal();
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
    console.log('Performing search with query:', this.searchQuery);
    this.bookService.getBooks().subscribe({
      next: (response: any) => { 
        // Vérifiez que 'response' est un objet avec une clé 'data' qui est un tableau
        if (response && Array.isArray(response.data)) {
          this.books = response.data; // Assigner le tableau de livres à 'this.books'
          
          // Appliquer les filtres
          this.filteredBooks = this.books.filter(book => {
            const matchesCategory = this.selectedCategory 
              ? book.categories.some(categorie => categorie.id === this.selectedCategory)
              : true;
            const matchesPromotion = this.filters.promotion ? book.remise > 0 : true;
            const matchesPrix = book.prix <= this.filters.prixMax;
            const matchesSearch = book.titre.toLowerCase().includes(this.searchQuery.toLowerCase()) || 
                                  book.auteur.toLowerCase().includes(this.searchQuery.toLowerCase());
            
            return matchesCategory && matchesPromotion && matchesPrix && matchesSearch;
          });
  
          this.updatePagination();
        } else {
          console.error('La réponse de l\'API est mal formatée ou la propriété data n\'est pas un tableau', response);
        }
      },
      error: (err) => console.error('Erreur lors de la récupération des livres', err)
    });
  }

  onSearchChange(): void {
    console.log('Search Query Changed:', this.searchQuery);
    this.filterBooks();
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

  // Gestion de la modale
  toggleModal(): void {
    this.isModalOpen = !this.isModalOpen;
  }

  onFileChange(event: any): void {
    const file = event.target.files[0];
    if (file) {
      const reader = new FileReader(); 
      reader.onload = () => {
        const base64String = reader.result as string; 
        this.newBook.photos = base64String;
      };
      reader.onerror = (error) => {
        console.error('Erreur lors de la conversion de l\'image en base64:', error);
      };
      reader.readAsDataURL(file); 
    }
  }
  
  addBook(): void {
    if (this.newBook && this.areAllFieldsFilled()) {
      this.newBook.categories = this.modalSelectedCategories.map(id => {
        return { id } as Categorie;
      });

      this.bookService.addBook(this.newBook as Book).subscribe({
        next: () => {
          this.loadBooks(); 
          this.toggleModal();

          // Afficher le message de succès
          this.showSuccessMessage = true;

          // Masquer le message après 3 secondes
          setTimeout(() => {
            this.showSuccessMessage = false;
          }, 3000); // 3 secondes
        },
        error: (err) => console.error('Erreur lors de l\'ajout du livre', err)
      });
    }
  }
  
  private loadBooks(): void {
    this.bookService.getBooks().subscribe({
      next: (response: any) => { 
        // Vérifiez que 'response' est un objet avec une clé 'data' qui est un tableau
        if (response && Array.isArray(response.data)) {
          this.books = response.data; // Assigner le tableau de livres à 'this.books'
          this.filteredBooks = [...this.books]; // Crée une copie pour le filtrage
          this.updatePagination();
          this.paginate();
        } else {
          console.error('La réponse de l\'API est mal formatée ou la propriété data n\'est pas un tableau', response);
        }
      },
      error: (err) => console.error('Erreur lors de la récupération des livres', err)
    });
  }
  

  openAddBookModal(): void {
    this.newBook = {
      id: 0,
      titre: '',
      auteur: '',
      prix: 0,
      remise: 0,
      isbn: '',
      editeur: '',
      datePublication: new Date(),
      estVendable: true,
      photos: '', // Assigne une chaîne vide
      categories: [],
      description: '',
    };
    this.isModalOpen = true;
  }

  private updatePagination(): void {
    this.totalPages = Math.ceil(this.filteredBooks.length / this.itemsPerPage);
    
    // Assurez-vous que `currentPage` est dans les limites valides
    if (this.currentPage > this.totalPages) {
      this.currentPage = this.totalPages;
    }
    
    if (this.currentPage < 1) {
      this.currentPage = 1;
    }
  
    this.paginate();
  }
  
  paginate(): void {
    if (Array.isArray(this.filteredBooks)) { 
      const startIndex = (this.currentPage - 1) * this.itemsPerPage;
      const endIndex = Math.min(this.currentPage * this.itemsPerPage, this.filteredBooks.length);
      this.paginatedBooks = this.filteredBooks.slice(startIndex, endIndex);
    } else {
      console.error('filteredBooks is not an array:', this.filteredBooks);
    }
  }
  
  previousPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.paginate();
    }
  }
  
  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      this.paginate();
    }
  }
  
  
  private updateItemsPerPage(): void {
    if (this.isBrowser) {
      const screenWidth = window.innerWidth;
      this.itemsPerPage = screenWidth < 768 ? 6 : 12;
      this.updatePagination();
    }
  }

  viewBookDetail(bookId: number): void {
    this.router.navigate(['/book-detail', bookId]);
  }

  getImageAltText(book: Book): string {
    return `${book.titre} - ${book.auteur}`;
  }

  // Ajout de la méthode pour vérifier si tous les champs requis sont remplis
  areAllFieldsFilled(): boolean {
    const { titre, auteur, prix, remise, isbn, editeur, datePublication, description, photos } = this.newBook;
    return !!(titre && auteur && prix !== undefined && remise !== undefined && isbn && editeur && datePublication && description && photos);
  }

  onCategoryChange(event: any): void {
    const categoryId = Number(event.target.value);
    const isChecked = event.target.checked;
  
    if (isChecked) {
      if (!this.modalSelectedCategories.includes(categoryId)) {
        this.modalSelectedCategories.push(categoryId);
      }
    } else {
      this.modalSelectedCategories = this.modalSelectedCategories.filter(id => id !== categoryId);
    }
  }
  
  closeModalOnOutsideClick(event: Event): void {
    this.isModalOpen = false; 
  }
}
