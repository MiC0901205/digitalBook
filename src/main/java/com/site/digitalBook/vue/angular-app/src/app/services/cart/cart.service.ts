import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { Book } from '../../interface/book.model';

@Injectable({
  providedIn: 'root'
})
export class CartService {
  private apiUrl = 'http://localhost:8080/api';
  private cartItemCountSubject = new BehaviorSubject<number>(0);

  constructor(private http: HttpClient) {}

  // Méthode pour obtenir le nombre d'articles du panier
  getCartItemCount$(userId: number): Observable<number> {
    return this.http.get<{ message: string, data: number }>(`${this.apiUrl}/cart/${userId}/count`).pipe(
      map(response => response.data),
      catchError(this.handleError)
    );
  }

  /**
   * Récupère les éléments du panier pour un utilisateur spécifique.
   * @param userId L'ID de l'utilisateur dont le panier est récupéré.
   * @returns Un Observable contenant les éléments du panier.
   */
  getCartItems(userId: number): Observable<Book[]> {
    return this.http.get<Book[]>(`${this.apiUrl}/cart/${userId}/items`).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Ajoute un livre au panier pour un utilisateur spécifique.
   * @param userId L'ID de l'utilisateur.
   * @param bookId L'ID du livre à ajouter.
   * @returns Un Observable contenant la réponse du backend.
   */
  addToCart(userId: number, bookId: number): Observable<any> {
    return this.sendCartRequest('add', userId, bookId);
  }

  /**
   * Supprime un livre du panier pour un utilisateur spécifique.
   * @param userId L'ID de l'utilisateur.
   * @param bookId L'ID du livre à supprimer.
   * @returns Un Observable contenant la réponse du backend.
   */
  removeFromCart(userId: number, bookId: number): Observable<any> {
    return this.sendCartRequest('remove', userId, bookId);
  }

  /**
   * Vide le panier pour un utilisateur spécifique.
   * @param userId L'ID de l'utilisateur.
   * @returns Un Observable contenant la réponse du backend.
   */
  clearCart(userId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/cart/clear`, null, {
      params: new HttpParams().set('userId', userId.toString())
    }).pipe(catchError(this.handleError));
  }

  /**
   * Méthode commune pour envoyer les requêtes d'ajout ou de suppression d'éléments dans le panier.
   * @param action 'add' ou 'remove'.
   * @param userId L'ID de l'utilisateur.
   * @param bookId L'ID du livre.
   * @returns Un Observable contenant la réponse du backend.
   */
  private sendCartRequest(action: 'add' | 'remove', userId: number, bookId: number): Observable<any> {
    const params = new HttpParams()
      .set('userId', userId.toString())
      .set('bookId', bookId.toString());

    return this.http.post(`${this.apiUrl}/cart/${action}`, null, { params }).pipe(
      catchError(this.handleError)
    );
  }

  /**
   * Gestion des erreurs HTTP.
   * @param error L'objet d'erreur retourné par HttpClient.
   * @returns Un Observable d'erreur.
   */
  private handleError(error: any): Observable<never> {
    console.error('Une erreur s\'est produite:', error);
    return throwError(() => new Error(error.message || 'Erreur inconnue'));
  }

  // Méthode pour obtenir le nombre d'articles dans le panier en tant qu'Observable
  getCartItemCountObservable(): Observable<number> {
    return this.cartItemCountSubject.asObservable();
  }

  // Méthode pour mettre à jour le nombre d'articles
  updateCartItemCount(userId: number): void {
    this.getCartItemCount$(userId).subscribe(count => {
      this.cartItemCountSubject.next(count);
    });
  }
}
