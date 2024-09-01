import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Commande } from '../../interface/commande.model';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private apiUrl = `http://localhost:8080/api`;

  constructor(private http: HttpClient) { }

  createCommande(commande: Commande): Observable<Commande> {
    return this.http.post<Commande>(`${this.apiUrl}/commande`, commande, {
      headers: { 'Content-Type': 'application/json' }
    });
  }   
}
