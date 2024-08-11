import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, of, tap } from 'rxjs';

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
    const token = localStorage.getItem('userToken');
    const headers = { 'Authorization': `Bearer ${token}` };
    
    return this.http.post<void>(`${this.apiUrl}/logout`, {}, { headers }).pipe(
      tap(() => {
        localStorage.removeItem('userToken');
      })
    );
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
