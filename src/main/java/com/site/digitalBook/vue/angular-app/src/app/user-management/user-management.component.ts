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

  constructor(private userService: AuthService) { }

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.userService.getUsers().pipe(
      map((data: User[]) => {
        console.log('Utilisateurs reçus:', data);
        this.totalPages = Math.ceil(data.length / this.itemsPerPage);
        this.enablePagination = this.totalPages > 1;
        this.updatePaginatedUsers(data);
        return data;
      }),
      catchError((err: any) => {
        console.error('Erreur lors du chargement des utilisateurs', err);
        return of([]);
      })
    ).subscribe(users => {
      this.paginatedUsers = users.slice(0, this.itemsPerPage);
    });
  }

  updatePaginatedUsers(users: User[]): void {
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    const endIndex = startIndex + this.itemsPerPage;
    this.paginatedUsers = users.slice(startIndex, endIndex);
  }

  prevPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
      this.loadUsers();
    }
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
      this.loadUsers();
    }
  }

  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
      this.loadUsers();
    }
  }

  openUserModal(user: User): void {
    this.selectedUser = user;
    console.log('Utilisateur sélectionné :', this.selectedUser);
    this.isModalOpen = true;
  }

  closeUserModal(): void {
    this.isModalOpen = false;
    this.selectedUser = undefined;
  }

  toggleUserStatus(event: Event): void {
    const checkbox = event.target as HTMLInputElement; // Cast pour obtenir la propriété checked
    if (this.selectedUser) {
      this.selectedUser.estActif = checkbox.checked; // Utilisation de la propriété checked de l'élément input
      console.log('Utilisateur mis à jour avant l\'appel du service:', this.selectedUser);

      // Appelle le service pour mettre à jour le profil de l'utilisateur
      this.userService.updateUserProfile(this.selectedUser).subscribe({
        next: (response) => {
          console.log('Réponse après mise à jour:', response);
          this.closeUserModal(); 
          this.loadUsers();
        },
        error: (err) => {
          console.error('Erreur lors de la mise à jour de l\'utilisateur', err);
        }
      });
    }
  }
}
