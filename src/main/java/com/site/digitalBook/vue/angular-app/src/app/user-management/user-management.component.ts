import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { AuthService } from '../services/auth/auth.service';
import { User } from '../interface/user.model';
import { NavbarComponent } from "../navbar/navbar.component";
import { FooterComponent } from "../footer/footer.component";

@Component({
  selector: 'app-user-management',
  standalone: true,
  templateUrl: './user-management.component.html',
  styleUrls: ['./user-management.component.css'],
  imports: [
    CommonModule,
    RouterModule,
    NavbarComponent,
    FooterComponent
  ]
})
export class UserManagementComponent implements OnInit {
  users$: Observable<User[]> = of([]);
  paginatedUsers: User[] = [];
  currentPage: number = 1;
  itemsPerPage: number = 5;
  totalPages: number = 1;
  enablePagination: boolean = false;

  selectedUser: User | undefined;
  isModalOpen: boolean = false;
  successMessage: string | null = null;  

  constructor(private userService: AuthService) { }

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.userService.getUsers().pipe(
      map((data: User[]) => {
        console.log("Données reçues du backend:", data); // Log pour vérifier les données
        this.totalPages = Math.ceil(data.length / this.itemsPerPage);
        this.enablePagination = this.totalPages > 1;
        return data; // Retourner les données directement
      }),
      catchError((err: any) => {
        console.error('Erreur lors du chargement des utilisateurs', err);
        return of([]);
      })
    ).subscribe(users => {
      // Mettre à jour les utilisateurs paginés ici
      this.updatePaginatedUsers(users); // Appeler updatePaginatedUsers ici
    });
  }

  updatePaginatedUsers(users: User[]): void {
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;
    
    // Log pour vérifier quels utilisateurs sont affichés
    console.log(`Affichage des utilisateurs de l'index ${startIndex} à ${endIndex}`);
    
    this.paginatedUsers = users.slice(startIndex, endIndex);
  }

  prevPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.loadUsers(); // Recharger les utilisateurs
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      this.loadUsers(); // Recharger les utilisateurs
    }
  }

  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      this.loadUsers(); // Recharger les utilisateurs
    }
  }

  openUserModal(user: User): void {
    this.selectedUser = user;
    this.isModalOpen = true;
  }

  closeUserModal(): void {
    this.isModalOpen = false;
    this.selectedUser = undefined;
  }

  toggleUserStatus(event: Event): void {
    const checkbox = event.target as HTMLInputElement;
    if (this.selectedUser) {
      const previousStatus = this.selectedUser.estActif;
      this.selectedUser.estActif = checkbox.checked;

      // Appelle le service pour mettre à jour le profil de l'utilisateur
      this.userService.updateUserProfile(this.selectedUser).subscribe({
        next: (response) => {
          // Vérifier la réponse du service
          if (response) {
            this.successMessage = `Le statut de ${this.selectedUser?.prenom} a été mis à jour avec succès.`;
          } else {
            console.error('Réponse du service vide ou incorrecte.');
          }

          // Fermer la modale et recharger les utilisateurs
          this.closeUserModal();
          this.loadUsers();

          setTimeout(() => {
            this.successMessage = null;
          }, 3000);
        },
        error: (err) => {
          console.error('Erreur lors de la mise à jour de l\'utilisateur', err);
        }
      });
    }
  }
}
