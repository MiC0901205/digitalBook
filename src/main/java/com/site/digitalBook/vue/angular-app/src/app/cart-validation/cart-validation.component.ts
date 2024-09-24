import { Component, OnInit, LOCALE_ID } from '@angular/core';
import { CartService } from '../services/cart/cart.service';
import { AuthService } from '../services/auth/auth.service';
import { OrderService } from '../services/order/order.service';
import { Book } from '../interface/book.model';
import { CommonModule, registerLocaleData } from '@angular/common';
import localeFr from '@angular/common/locales/fr';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { Commande } from '../interface/commande.model';
import { PaymentCard } from '../interface/paymentCard.model';
import { PaymentService } from '../services/payment/payment.service';

registerLocaleData(localeFr);

@Component({
  selector: 'app-cart-validation',
  templateUrl: './cart-validation.component.html',
  styleUrls: ['./cart-validation.component.css'],
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule
  ],
  providers: [
    { provide: LOCALE_ID, useValue: 'fr' }
  ]
})
export class CartValidationComponent implements OnInit {
  cartItems: Book[] = [];
  total: number = 0;
  userId?: number;
  selectedPaymentMethod: string = '';
  showPaymentModal: boolean = true;
  paymentForm!: FormGroup;
  showSuccessMessage: boolean = false;
  savedCards: PaymentCard[] = []; // Ajout de la propriété pour les cartes enregistrées

  constructor(
    private cartService: CartService,
    private authService: AuthService,
    private fb: FormBuilder,
    private orderService: OrderService,
    private paymentService: PaymentService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.paymentForm = this.fb.group({
      paypalEmail: [''],
      cardNumber: ['', [Validators.pattern(/^\d{4}\s\d{4}\s\d{4}\s\d{4}$/)]],
      expiryDate: ['', [Validators.pattern(/^\d{2}\/\d{2}$/)]],
      cvv: ['', [Validators.pattern(/^\d{3}$/)]],
      giftCardCode: [''],
      saveCard: [false], // Ajout de la case à cocher ici
      selectedCard: [null], // Ajout d'un FormControl pour la carte sélectionnée
      submit: [false] // FormControl pour le bouton de soumission
    });
    
  
    this.authService.getUserId().subscribe({
      next: (id: number) => {
        this.userId = id;
        this.loadCartItems();
        this.loadSavedCards(); // Ajoute cet appel ici
      },
      error: (err: any) => console.error('Erreur lors de la récupération de l\'ID utilisateur', err)
    });
  }
  

  loadCartItems(): void {
    if (this.userId !== undefined) {
      this.cartService.getCartItems(this.userId).subscribe({
        next: (response: any) => {
          this.cartItems = response.data || [];
          this.calculateTotal();
        },
        error: (err: any) => console.error('Erreur lors de la récupération des éléments du panier', err)
      });
    }
  }

  calculateTotal(): void {
    this.total = this.cartItems.reduce((acc, item) => {
      const discountedPrice = this.calculateDiscountedPrice(item.prix, item.remise);
      return acc + discountedPrice;
    }, 0);
  }
  
  calculateDiscountedPrice(price: number, discount: number): number {
    if (discount > 0) {
      return parseFloat((price * (1 - discount / 100)).toFixed(2));
    }
    return price;
  }
  
  selectPaymentMethod(method: string): void {
    this.selectedPaymentMethod = method;
    this.resetForm();
  }

  closePaymentModal(): void {
    this.showPaymentModal = false;
  }

  loadSavedCards(): void {
    if (this.userId !== undefined) {
      this.paymentService.getSavedCards(this.userId).subscribe({
        next: (response: any) => { // Utilise 'any' pour obtenir plus de flexibilité
          console.log('Réponse de l\'API pour les cartes:', response);
          this.savedCards = response.data || []; // Assigne le tableau à partir de 'data'
          console.log('Cartes enregistrées:', this.savedCards);
        },
        error: (err: any) => console.error('Erreur lors de la récupération des cartes enregistrées', err)
      });
    }
  }

  maskCardNumber(cardNumber: string): string {
    // Décoder le numéro de carte (en supposant qu'il est encodé en Base64)
    const decodedCardNumber = atob(cardNumber); // Utilise atob pour décoder

    // Masquer tous les chiffres sauf les 2 premiers et les 4 derniers
    return decodedCardNumber.slice(0, 2) + '** **** **** **' + decodedCardNumber.slice(-2);
  }


  maskCvv(cvv: string): string {
    // Décodez le CVV (en supposant qu'il est encodé en Base64)
    return atob(cvv); // Utilise atob pour décoder
  }


  selectCard(card: PaymentCard): void {
    const decryptedCardNumber = atob(card.cardNumber); // Déchiffrez le numéro de carte
    this.paymentForm.patchValue({
        selectedCard: card,
        cardNumber: this.formatCardNumberWithSpaces(decryptedCardNumber), // Formatez avec des espaces
        expiryDate: card.expiryDate,
        cvv: atob(card.cvv) // Affichez le CVV déchiffré
    });
    
    this.checkIfSubmitEnabled();
    console.log("Carte sélectionnée:", card);
    console.log("État du bouton de soumission:", this.paymentForm.get('submit')?.value);
}

  // Fonction pour formater le numéro de carte avec des espaces
  private formatCardNumberWithSpaces(cardNumber: string): string {
      return cardNumber.replace(/(\d{4})(?=\d)/g, '$1 '); // Ajoute un espace tous les 4 chiffres
  }


 checkIfSubmitEnabled(): void {
    const isCardSelected = this.paymentForm.get('selectedCard')?.value !== null;
    const isFormValid = this.paymentForm.valid;
    console.log('Carte sélectionnée:', isCardSelected);
    console.log('Formulaire valide:', isFormValid);
    this.paymentForm.get('submit')?.setValue(isCardSelected && isFormValid);
  }

  async submitOrder(): Promise<void> {
    if (this.userId !== undefined && this.paymentForm.valid) {
      const currentDate = new Date();
      const offset = currentDate.getTimezoneOffset();
      const localDate = new Date(currentDate.getTime() - offset * 60 * 1000);
      
      const commande: Commande = {
        user: { id: this.userId },
        prixTotal: this.total,
        methodePaiement: this.selectedPaymentMethod,
        dateCreation: localDate.toISOString().slice(0, 19),
        livreIds: this.cartItems.map(item => item.id)
      };
      
      try {
        const response = await this.orderService.createCommande(commande).toPromise();
        
        const saveCard = this.paymentForm.get('saveCard')?.value;
        console.log("Mémoriser cette carte :", saveCard);
        
        if (saveCard) {
          const paymentCard: PaymentCard = {
            cardNumber: this.paymentForm.get('cardNumber')?.value.replace(/\s/g, ''),
            expiryDate: this.paymentForm.get('expiryDate')?.value,
            cvv: this.paymentForm.get('cvv')?.value,
            userId: this.userId
          };
  
          console.log("Données de la carte de paiement :", paymentCard);
          
          await this.paymentService.sendPaymentCard(paymentCard.cardNumber, paymentCard.expiryDate, paymentCard.cvv, paymentCard.userId!).toPromise();
        }
        
        this.downloadPdfs();
        
        this.cartService.clearCart(this.userId!).subscribe({
          next: () => {
            this.cartService.updateCartItemCount(this.userId!);
          },
          error: (err) => console.error('Erreur lors du vidage du panier:', err)
        });
        
        this.showSuccessMessage = true;
        setTimeout(() => {
          this.router.navigate(['/']);
        }, 3000);
        
      } catch (err) {
        console.error('Erreur lors de la création de la commande:', err);
      }
    }
  }
  
  formatCvv(event: Event): void {
    const input = event.target as HTMLInputElement;
    let value = input.value.replace(/\D/g, '');

    if (value.length > 3) {
      value = value.slice(0, 3);
    }

    input.value = value;
    this.paymentForm.get('cvv')?.setValue(value);
  }

  formatCardNumber(event: Event): void {
    const input = event.target as HTMLInputElement;
    let value = input.value.replace(/\D/g, '');

    if (value.length > 16) {
      value = value.slice(0, 16);
    }
    value = value.match(/.{1,4}/g)?.join(' ') || value;

    input.value = value;
    this.paymentForm.get('cardNumber')?.setValue(value);
  }

  formatExpiryDate(event: Event): void {
    const input = event.target as HTMLInputElement;
    let value = input.value.replace(/\D/g, '');

    if (value.length > 4) {
      value = value.slice(0, 4);
    }

    if (value.length > 2) {
      value = `${value.slice(0, 2)}/${value.slice(2)}`;
    }

    input.value = value;
    this.paymentForm.get('expiryDate')?.setValue(value);
  }

  resetForm(): void {
    this.paymentForm.reset();

    if (this.selectedPaymentMethod === 'paypal') {
      this.paymentForm.get('paypalEmail')?.setValidators([Validators.required, Validators.email]);
      this.paymentForm.get('paypalEmail')?.updateValueAndValidity();
    } else {
      this.paymentForm.get('paypalEmail')?.clearValidators();
    }

    if (this.selectedPaymentMethod === 'creditCard') {
      this.paymentForm.get('cardNumber')?.setValidators([Validators.required, Validators.pattern(/^\d{4}\s\d{4}\s\d{4}\s\d{4}$/)]);
      this.paymentForm.get('expiryDate')?.setValidators([Validators.required, Validators.pattern(/^\d{2}\/\d{2}$/)]);
      this.paymentForm.get('cvv')?.setValidators([Validators.required, Validators.pattern(/^\d{3}$/)]);
    } else {
      this.paymentForm.get('cardNumber')?.clearValidators();
      this.paymentForm.get('expiryDate')?.clearValidators();
      this.paymentForm.get('cvv')?.clearValidators();
    }

    if (this.selectedPaymentMethod === 'giftCard') {
      this.paymentForm.get('giftCardCode')?.setValidators([Validators.required]);
    } else {
      this.paymentForm.get('giftCardCode')?.clearValidators();
    }

    this.paymentForm.updateValueAndValidity();
  }

  downloadPdfs(): void {
    this.cartItems.forEach(item => {
      const pdfUrl = this.generatePdfUrl(item.titre);

      if (pdfUrl) {
        const link = document.createElement('a');
        link.href = pdfUrl;
        link.download = `${item.titre}.pdf`;

        document.body.appendChild(link);

        link.click();

        document.body.removeChild(link);
      }
    });
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

  validatePayment() {
    const cardNumber = '5455655548485451';
    const expiryDate = '06/26';
    const cvv = '455';
    const userId = 32;

    // Appel au service pour envoyer les données de paiement
    this.paymentService.sendPaymentCard(cardNumber, expiryDate, cvv, userId);
  }

  // Supprimer une carte
  deleteCard(card: any): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cette carte ?')) {
      this.paymentService.deletePaymentCard(card.id).subscribe({
        next: () => {
          // Mise à jour de la liste des cartes sauvegardées
          this.savedCards = this.savedCards.filter(c => c.id !== card.id);
          alert('Carte supprimée avec succès.');
          this.loadSavedCards(); // Recharger les cartes depuis l'API
        },
        error: (err: any) => {
          console.error('Erreur lors de la suppression de la carte :', err);
          alert('Une erreur est survenue lors de la suppression de la carte.');
        }
      });
    }
  }
  
}
