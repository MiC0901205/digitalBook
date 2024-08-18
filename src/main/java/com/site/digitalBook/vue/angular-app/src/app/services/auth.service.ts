import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { catchError, Observable, of, tap, throwError } from 'rxjs';
import { User } from '../interface/user.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private apiUrl = 'http://localhost:8080/api'; 
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

  setLoggedInStatus(isLoggedIn: boolean): void {
    if (isLoggedIn) {
      localStorage.setItem('userToken', 'dummy-token');
    } else {
      localStorage.removeItem('userToken');
    }
  }

  forgotPassword(email: string): Observable<any> {
    const user = { email }; 
    return this.http.post(`${this.apiUrl}/forgot-password`, user);
  }
  
  resetPassword(email: string, newPassword: string): Observable<any> {
    const body = { newPassword }; 
    return this.http.post(`${this.apiUrl}/reset-password`, body, {
      params: { email }
    });
  }

  getCurrentUser(): Observable<{ data: User }> {
    const email = localStorage.getItem('userEmail');
    console.log("email", email);
    if (!email) {
      throw new Error('Email non trouvé dans le localStorage');
    }

    const headers = new HttpHeaders({
      'Email': email
    });

    return this.http.get<{ data: User }>(`${this.apiUrl}/current-user`, { headers });
  }
  
  updateUserProfile(user: User): Observable<User> {
    console.log("user", user);
    const token = localStorage.getItem('userToken');
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });
    return this.http.put<User>(`${this.apiUrl}/user/${user.id}`, user, { headers });
  }
}
