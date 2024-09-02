import { Component, OnInit, LOCALE_ID } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth/auth.service';
import { OrderService } from '../services/order/order.service';
import { BookService } from '../services/book/book.service';
import { Commande } from '../interface/commande.model';
import { Book } from '../interface/book.model';
import { Observable, forkJoin } from 'rxjs';
import { CommonModule, registerLocaleData, CurrencyPipe } from '@angular/common';
import localeFr from '@angular/common/locales/fr';
import { FooterComponent } from '../footer/footer.component';
import { NavbarComponent } from '../navbar/navbar.component';

registerLocaleData(localeFr);

@Component({
  selector: 'app-order-history',
  templateUrl: './order-history.component.html',
  styleUrls: ['./order-history.component.css'],
  standalone: true,
  imports: [
    CommonModule,
    FooterComponent,
    NavbarComponent
  ],
  providers: [
    { provide: LOCALE_ID, useValue: 'fr' },
    CurrencyPipe
  ]
})
export class OrderHistoryComponent implements OnInit {
  commandes$: Observable<Commande[]> | undefined;
  sortedCommandes: Commande[] = [];
  displayedCommandes: Commande[] = [];
  selectedCommande: Commande | undefined;
  livres: Book[] = [];
  isModalOpen = false;
  currentPage: number = 1;
  itemsPerPage: number = 20;

  sortByDateAscending = true;
  sortByPriceAscending = true;

  get totalPages(): number {
    return Math.ceil(this.sortedCommandes.length / this.itemsPerPage);
  }

  constructor(
    private authService: AuthService,
    private orderService: OrderService,
    private bookService: BookService,
    private router: Router,
    private currencyPipe: CurrencyPipe
  ) {}

  ngOnInit(): void {
    this.authService.getUserId().subscribe({
      next: (userId: number) => {
        this.commandes$ = this.orderService.getCommandesByUserId(userId);
        this.commandes$.subscribe({
          next: (commandes) => {
            console.log('Commandes reçues :', commandes);
            this.sortedCommandes = commandes;
            this.updateDisplayedCommandes();
          },
          error: (err) => {
            console.error('Erreur lors de la récupération des commandes', err);
          }
        });
      },
      error: (err: any) => {
        console.error('Erreur lors de la récupération de l\'ID utilisateur', err);
        this.router.navigate(['/login']);
      }
    });
  }

  updateDisplayedCommandes(): void {
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;
    this.displayedCommandes = this.sortedCommandes.slice(startIndex, endIndex);
  }

  openOrderDetails(commande: Commande): void {
    this.selectedCommande = commande;
    this.livres = [];

    if (commande.livreIds && commande.livreIds.length > 0) {
      const livreObservables = commande.livreIds.map(id => this.bookService.getBookById(id));
      forkJoin(livreObservables).subscribe(
        (livres: Book[]) => {
          this.livres = livres;
          this.isModalOpen = true;
        },
        (error) => {
          console.error('Erreur lors de la récupération des livres', error);
        }
      );
    } else {
      this.isModalOpen = true;
    }
  }

  closeOrderDetails(): void {
    this.isModalOpen = false;
    this.selectedCommande = undefined;
  }

  downloadPdfs(): void {
    console.log('Démarrage du processus de téléchargement des PDFs...');
    this.livres.forEach(livre => {
      const pdfUrl = this.generatePdfUrl(livre.titre);
      console.log('Traitement du livre:', livre.titre, 'avec l\'URL PDF générée:', pdfUrl);

      if (pdfUrl) {
        const link = document.createElement('a');
        link.href = pdfUrl;
        link.download = `${livre.titre}.pdf`;

        console.log('Ajout du lien au DOM');
        document.body.appendChild(link);

        console.log('Déclenchement du téléchargement');
        link.click();

        console.log('Suppression du lien du DOM');
        document.body.removeChild(link);

        console.log(`PDF téléchargé pour ${livre.titre}`);
      } else {
        console.log(`Aucune URL PDF générée pour ${livre.titre}`);
      }
    });
    console.log('Processus de téléchargement des PDFs terminé.');
  }

  generatePdfUrl(titre: string): string {
    const specialChars = /['"]/g;

    let formattedTitle = titre;

    if (specialChars.test(titre)) {
      formattedTitle = titre
        .replace(/'/g, '')
        .replace(/"/g, '')
        .replace(/\s+/g, '-');
    } else {
      formattedTitle = titre.replace(/\s+/g, '-');
    }

    return `assets/pdfs/${formattedTitle}.pdf`;
  }

  formatPrice(price: number): string | null {
    return this.currencyPipe.transform(price, 'EUR', 'symbol', '1.2-2');
  }

  // Pagination Methods
  previousPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.updateDisplayedCommandes();
    }
  }

  nextPage(): void {
    if (this.currentPage * this.itemsPerPage < this.sortedCommandes.length) {
      this.currentPage++;
      this.updateDisplayedCommandes();
    }
  }

  // Sorting Methods
  toggleSortByDate(): void {
    this.sortByDateAscending = !this.sortByDateAscending;
    this.sortedCommandes.sort((a, b) => 
      this.sortByDateAscending 
        ? new Date(a.dateCreation).getTime() - new Date(b.dateCreation).getTime()
        : new Date(b.dateCreation).getTime() - new Date(a.dateCreation).getTime()
    );
    this.updateDisplayedCommandes();
  }

  toggleSortByPrice(): void {
    this.sortByPriceAscending = !this.sortByPriceAscending;
    this.sortedCommandes.sort((a, b) => 
      this.sortByPriceAscending 
        ? (a.prixTotal || 0) - (b.prixTotal || 0)
        : (b.prixTotal || 0) - (a.prixTotal || 0)
    );
    this.updateDisplayedCommandes();
  }
}
