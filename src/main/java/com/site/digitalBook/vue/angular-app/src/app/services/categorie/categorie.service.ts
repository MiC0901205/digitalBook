import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Categorie } from '../../interface/categorie.model'; // Assurez-vous que le modèle Categorie existe et est correct

@Injectable({
  providedIn: 'root'
})
export class CategorieService {

  private apiUrl = 'http://localhost:8080/api'; 

  constructor(private http: HttpClient) { }

  getCategories(): Observable<any> {
    return this.http.get<Categorie>(`${this.apiUrl}/categories`).pipe(
      catchError(this.handleError)
    );
  }

  // Gestion des erreurs HTTP
  private handleError(error: any): Observable<never> {
    console.error('Une erreur s\'est produite:', error);
    return throwError(() => new Error(error.message || 'Erreur inconnue'));
  }
}
