import { Routes } from '@angular/router';
import { HomeComponent } from './home/home.component';
import { LoginComponent } from './login/login.component';
import { RegisterComponent } from './register/register.component';
import { UserActionComponent } from './user-action/user-action.component';
import { ProfileComponent } from './profile/profile.component';
import { BookDisplayComponent } from './book-display/book-display.component';
import { FooterComponent } from './footer/footer.component'; 
import { BookDetailComponent } from './book-detail/book-detail.component';
import { CartComponent } from './cart/cart.component';
import { CartValidationComponent } from './cart-validation/cart-validation.component';
import { OrderHistoryComponent } from './order-history/order-history.component';
import { AuthGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'user-action', component: UserActionComponent },
  { path: 'reset-password', component: UserActionComponent },
  { path: 'profil', component: ProfileComponent, canActivate: [AuthGuard]},
  { path: 'books', component: BookDisplayComponent },
  { path: 'footer/privacy', component: FooterComponent },
  { path: 'footer/terms', component: FooterComponent },
  { path: 'footer/cookies', component: FooterComponent },
  { path: 'footer/retraction', component: FooterComponent },
  { path: 'footer/sales-conditions', component: FooterComponent },
  { path: 'book-detail/:id', component: BookDetailComponent },
  { path: 'cart', component: CartComponent },
  { path: 'cart-validation', component: CartValidationComponent },
  { path: 'order-history', component: OrderHistoryComponent, canActivate: [AuthGuard]}, 
  { path: '**', redirectTo: '' }
];


