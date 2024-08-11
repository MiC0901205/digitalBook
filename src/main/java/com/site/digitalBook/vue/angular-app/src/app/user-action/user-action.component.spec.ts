import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UserActionComponent } from './user-action.component';

describe('UserActionComponent', () => {
  let component: UserActionComponent;
  let fixture: ComponentFixture<UserActionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UserActionComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(UserActionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
