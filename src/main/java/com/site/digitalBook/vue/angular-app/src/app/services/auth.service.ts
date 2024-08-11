import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private apiUrl = 'http://localhost:8080/api'; // L'URL de votre backend
  private userEmail: string | null = null;

  constructor(private http: HttpClient) { }

  login(email: string, mdp: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, { email, mdp });
  }

  register(user: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, user);
  }

  verifyCode(email: string, code: string): Observable<any> {
    const body = { email, code };
    return this.http.post(`${this.apiUrl}/verify-code`, body, {
      headers: { 'Content-Type': 'application/json' }
    });
  }
  
  setEmail(email: string) {
    this.userEmail = email;
  }

  getEmail(): string | null {
    return this.userEmail;
  }

  isLoggedIn(): Observable<boolean> {
    return of(!!localStorage.getItem('userToken'));
  }

  logout(): Observable<void> {
    // Implémentez la logique de déconnexion ici, par exemple, effacer le token
    localStorage.removeItem('userToken');
    return of();
  }

  // Ajoutez cette méthode si elle est nécessaire
  setLoggedInStatus(isLoggedIn: boolean): void {
    if (isLoggedIn) {
      localStorage.setItem('userToken', 'dummy-token');
    } else {
      localStorage.removeItem('userToken');
    }
  }
}
