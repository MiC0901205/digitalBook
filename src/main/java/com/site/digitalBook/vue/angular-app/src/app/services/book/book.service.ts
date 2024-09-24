import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { Book } from '../../interface/book.model';

@Injectable({
  providedIn: 'root'
})
export class BookService {

  private apiUrl = 'http://localhost:8080/api'; 

  constructor(private http: HttpClient) { }

  // Méthode pour récupérer tous les livres
  getBooks(searchQuery?: string): Observable<Book[]> {
    let url = `${this.apiUrl}/books`;
    if (searchQuery) {
      url += `?search=${encodeURIComponent(searchQuery)}`;
    }
    return this.http.get<Book[]>(url).pipe(
      catchError(this.handleError)
    );
  }

  getBookById(id: number): Observable<{ data: Book }> {
    return this.http.get<{ data: Book }>(`${this.apiUrl}/books/${id}`).pipe(
      map(response => response), 
      catchError(this.handleError)
    );
  }
  
  

  // Méthode pour récupérer les livres par catégorie
  getBooksByCategory(categoryName: string): Observable<Book[]> {
    return this.http.get<Book[]>(`${this.apiUrl}/books/category/${categoryName}`).pipe(
      catchError(this.handleError)
    );
  }

  updateBook(id: number, updateData: { remise: number }): Observable<Book> {
    return this.http.put<Book>(
      `${this.apiUrl}/book/${id}`,
      updateData,
      { headers: { 'Content-Type': 'application/json' } }
    ).pipe(
      catchError(this.handleError)
    );
  }

  addBook(book: Book): Observable<Book> {  
    const headers = new HttpHeaders({
      'Content-Type': 'application/json'
    });
  
    return this.http.post<Book>('http://localhost:8080/api/book', book, { headers });
  }

  // Gestion des erreurs HTTP
  private handleError(error: any): Observable<never> {
    console.error('Une erreur s\'est produite:', error);
    return throwError(() => new Error(error.message || 'Erreur inconnue'));
  }
}
