// book.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Book } from '../../interface/book.model'; // Assurez-vous que le modèle Book existe et est correct

@Injectable({
  providedIn: 'root'
})
export class BookService {

  private apiUrl = 'http://localhost:8080/api'; 

  constructor(private http: HttpClient) { }

  // Méthode pour récupérer tous les livres
  getBooks(): Observable<Book[]> {
    return this.http.get<Book[]>(`${this.apiUrl}/books`).pipe(
      catchError(this.handleError)
    );
  }

  // Méthode pour récupérer un livre par son ID
  getBookById(id: number): Observable<Book> {
    return this.http.get<Book>(`${this.apiUrl}/books/${id}`);
  }

  // Méthode pour récupérer les livres par catégorie
  getBooksByCategory(categoryName: string): Observable<Book[]> {
    return this.http.get<Book[]>(`${this.apiUrl}/books/category/${categoryName}`).pipe(
      catchError(this.handleError)
    );
  }

  // Gestion des erreurs HTTP
  private handleError(error: any): Observable<never> {
    console.error('Une erreur s\'est produite:', error);
    return throwError(() => new Error(error.message || 'Erreur inconnue'));
  }
}
