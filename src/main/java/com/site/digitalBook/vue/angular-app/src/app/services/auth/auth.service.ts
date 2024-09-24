import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { User } from '../../interface/user.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private apiUrl = 'http://localhost:8080/api'; 
  private userEmail: string | null = null;

  private isLoggedInSubject: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(this.hasToken());
  public isLoggedIn$ = this.isLoggedInSubject.asObservable();

  constructor(private http: HttpClient) { }

  private getLocalStorage() {
    return typeof window !== 'undefined' ? window.localStorage : null;
  }

  private hasToken(): boolean {
    const localStorage = this.getLocalStorage();
    const token = localStorage ? localStorage.getItem('userToken') : null;
    return !!token;
  }

  login(email: string, mdp: string): Observable<any> {
    return this.http.post(`${this.apiUrl}/login`, { email, mdp }).pipe(
      tap((response: any) => {
        if (response && response.token) {
          const localStorage = this.getLocalStorage();
          if (localStorage) {
            localStorage.setItem('userToken', response.token);
            localStorage.setItem('userProfile', response.data.profil);
            localStorage.setItem('userEmail', email);  
            this.isLoggedInSubject.next(true);
          }
        }
      }),
    );
  }

  register(user: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, user).pipe(
      catchError(error => {
        console.error('Erreur lors de l\'enregistrement:', error);
        return throwError(() => new Error('Registration failed'));
      })
    );
  }

  verifyCode(email: string, code: string): Observable<any> {
    const body = { email, code };
    return this.http.post(`${this.apiUrl}/verify-code`, body, {
      headers: { 'Content-Type': 'application/json' }
    }).pipe(
      catchError(error => {
        console.error('Erreur lors de la vérification du code:', error);
        return throwError(() => new Error('Code verification failed'));
      })
    );
  }
  
  setEmail(email: string) {
    this.userEmail = email;
  }

  getEmail(): string | null {
    return this.userEmail;
  }

  isLoggedIn(): Observable<boolean> {
    return this.isLoggedIn$;
  }

  logout(): Observable<void> {
    const localStorage = this.getLocalStorage();
    const token = localStorage ? localStorage.getItem('userToken') : null;
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    
    return this.http.post<void>(`${this.apiUrl}/logout`, {}, { headers }).pipe(
      tap(() => {
        if (localStorage) {
          localStorage.removeItem('userToken');
          this.isLoggedInSubject.next(false);
        }
      }),
    );
  }

  forgotPassword(email: string): Observable<any> {
    const user = { email }; 
    return this.http.post(`${this.apiUrl}/forgot-password`, user).pipe(
      catchError(error => {
        console.error('Erreur lors de l\'envoi de l\'email de réinitialisation de mot de passe:', error);
        return throwError(() => new Error('Forgot password failed'));
      })
    );
  }
  
  resetPassword(email: string, newPassword: string): Observable<any> {
    const body = { newPassword }; 
    return this.http.post(`${this.apiUrl}/reset-password`, body, {
      params: { email }
    }).pipe(
      catchError(error => {
        console.error('Erreur lors de la réinitialisation du mot de passe:', error);
        return throwError(() => new Error('Password reset failed'));
      })
    );
  }

  getCurrentUser(): Observable<{ data: User }> {
    const localStorage = this.getLocalStorage();
    const email = localStorage ? localStorage.getItem('userEmail') : null;
    if (!email) {
      return throwError(() => new Error('Email non trouvé dans le localStorage'));
    }
  
    const headers = new HttpHeaders({
      'Email': email
    });
  
    return this.http.get<{ data: User }>(`${this.apiUrl}/current-user`, { headers }).pipe(
      catchError(error => {
        console.error('Erreur lors de la récupération de l\'utilisateur courant:', error);
        return throwError(() => new Error('Failed to fetch current user'));
      })
    );
  }
  
  updateUserProfile(user: User): Observable<User> {
    const localStorage = this.getLocalStorage();
    const token = localStorage ? localStorage.getItem('userToken') : null;
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });
    return this.http.put<User>(`${this.apiUrl}/user/${user.id}`, user, { headers }).pipe(
      catchError(error => {
        console.error('Erreur lors de la mise à jour de l\'utilisateur:', error);
        return throwError(() => new Error('User update failed'));
      })
    );
  }

  getUserId(): Observable<number> {
    return this.getCurrentUser().pipe(
      map(response => {
        if (!response.data || !response.data.id) {
          throw new Error('ID utilisateur non disponible');
        }
        return response.data.id;
      }),
      catchError(err => {
        console.error('Erreur lors de la récupération de l\'ID utilisateur', err);
        return throwError(() => new Error('Erreur lors de la récupération de l\'ID utilisateur'));
      })
    );
  }

  setLoggedInStatus(isLoggedIn: boolean): void {
    const localStorage = this.getLocalStorage();
    if (localStorage) {
      if (isLoggedIn) {
        localStorage.setItem('userToken', 'dummy-token');
        this.isLoggedInSubject.next(true);
      } else {
        localStorage.removeItem('userToken');
        this.isLoggedInSubject.next(false);
      }
    }
  }

  getUserProfile(): string | null {
    const localStorage = this.getLocalStorage();
    return localStorage ? localStorage.getItem('userProfile') : null;
  }

  getUsers(): Observable<User[]> {
    const localStorage = this.getLocalStorage();
    const token = localStorage ? localStorage.getItem('userToken') : null;
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
  
    return this.http.get<User[]>(`${this.apiUrl}/users`, { headers }).pipe(
      catchError(error => {
        console.error('Erreur lors de la récupération des utilisateurs:', error);
        return throwError(() => new Error('Failed to fetch users'));
      })
    );
  }
  
  sendContactMessage(email: string, subject: string, message: string): Observable<any> {
    // Construire les paramètres de la requête
    const params = new HttpParams()
      .set('email', email)
      .set('subject', subject)
      .set('message', message);
  
    return this.http.post(`${this.apiUrl}/contact`, null, { params }).pipe(
      catchError(error => {
        console.error('Erreur lors de l\'envoi du message de contact:', error);
        return throwError(() => new Error('Échec de l\'envoi du message de contact'));
      })
    );
  }
  
}
