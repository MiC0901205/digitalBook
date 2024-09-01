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

  constructor(
    private cartService: CartService,
    private authService: AuthService,
    private fb: FormBuilder,
    private orderService: OrderService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.paymentForm = this.fb.group({
      paypalEmail: [''],
      cardNumber: ['', [Validators.pattern(/^\d{4}\s\d{4}\s\d{4}\s\d{4}$/)]],
      expiryDate: ['', [Validators.pattern(/^\d{2}\/\d{2}$/)]],
      cvv: ['', [Validators.pattern(/^\d{3}$/)]],
      giftCardCode: ['']
    });

    this.authService.getUserId().subscribe({
      next: (id: number) => {
        this.userId = id;
        this.loadCartItems();
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
    this.total = this.cartItems.reduce((acc, item) => acc + (item.prix || 0), 0);
  }

  selectPaymentMethod(method: string): void {
    this.selectedPaymentMethod = method;
    this.resetForm();
  }

  closePaymentModal(): void {
    this.showPaymentModal = false;
  }

  async submitOrder(): Promise<void> {
    if (this.userId !== undefined && this.paymentForm.valid) {
      const commande: Commande = {
        user: { id: this.userId },
        prixTotal: this.total,
        methodePaiement: this.selectedPaymentMethod,
        dateCreation: new Date().toISOString(),
        livreIds: this.cartItems.map(item => item.id)
      };

      try {
        const response = await this.orderService.createCommande(commande).toPromise();
        console.log('Commande créée avec succès:', response);

        // Télécharge les PDFs associés aux livres
        this.downloadPdfs();

        this.cartService.clearCart(this.userId!).subscribe({
          next: () => {
            console.log('Panier vidé avec succès');
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
    } else {
      console.log('Formulaire invalide');
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
    console.log('Mode de paiement sélectionné:', this.selectedPaymentMethod);

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
    console.log('Starting PDF download process...');
    this.cartItems.forEach(item => {
      const pdfUrl = this.generatePdfUrl(item.titre);
      console.log('Processing book:', item.titre, 'with generated PDF URL:', pdfUrl);
  
      if (pdfUrl) {
        const link = document.createElement('a');
        link.href = pdfUrl;
        link.download = `${item.titre}.pdf`; 
  
        console.log('Adding link to DOM');
        document.body.appendChild(link);
  
        console.log('Triggering download');
        link.click();
  
        console.log('Removing link from DOM');
        document.body.removeChild(link);
  
        console.log(`Downloaded PDF for ${item.titre}`);
      } else {
        console.log(`No PDF URL generated for ${item.titre}`);
      }
    });
    console.log('PDF download process finished.');
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
}
