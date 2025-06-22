import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ProjectService } from '../../../services/project.service';
import { Project } from '../../../models/project.model';

@Component({
  selector: 'app-project-form',
  template: `
    <div class="project-form-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>{{ isEditMode ? 'Edit Project' : 'Create New Project' }}</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <form [formGroup]="projectForm" (ngSubmit)="onSubmit()">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Name</mat-label>
              <input matInput formControlName="name" required>
              <mat-error *ngIf="projectForm.get('name')?.hasError('required')">
                Name is required
              </mat-error>
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Description</mat-label>
              <textarea matInput formControlName="description" rows="4" required></textarea>
              <mat-error *ngIf="projectForm.get('description')?.hasError('required')">
                Description is required
              </mat-error>
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Status</mat-label>
              <mat-select formControlName="status" required>
                <mat-option value="PLANNING">Planning</mat-option>
                <mat-option value="ACTIVE">Active</mat-option>
                <mat-option value="ON_HOLD">On Hold</mat-option>
                <mat-option value="COMPLETED">Completed</mat-option>
              </mat-select>
              <mat-error *ngIf="projectForm.get('status')?.hasError('required')">
                Status is required
              </mat-error>
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Start Date</mat-label>
              <input matInput [matDatepicker]="startPicker" formControlName="startDate" required>
              <mat-datepicker-toggle matSuffix [for]="startPicker"></mat-datepicker-toggle>
              <mat-datepicker #startPicker></mat-datepicker>
              <mat-error *ngIf="projectForm.get('startDate')?.hasError('required')">
                Start date is required
              </mat-error>
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>End Date</mat-label>
              <input matInput [matDatepicker]="endPicker" formControlName="endDate" required>
              <mat-datepicker-toggle matSuffix [for]="endPicker"></mat-datepicker-toggle>
              <mat-datepicker #endPicker></mat-datepicker>
              <mat-error *ngIf="projectForm.get('endDate')?.hasError('required')">
                End date is required
              </mat-error>
            </mat-form-field>

            <div class="form-actions">
              <button mat-button type="button" (click)="goBack()">Cancel</button>
              <button mat-raised-button color="primary" type="submit" [disabled]="projectForm.invalid">
                {{ isEditMode ? 'Update' : 'Create' }}
              </button>
            </div>
          </form>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .project-form-container {
      padding: 20px;
      max-width: 800px;
      margin: 0 auto;
    }
    .full-width {
      width: 100%;
      margin-bottom: 15px;
    }
    .form-actions {
      display: flex;
      justify-content: flex-end;
      gap: 10px;
      margin-top: 20px;
    }
  `]
})
export class ProjectFormComponent implements OnInit {
  projectForm: FormGroup;
  isEditMode = false;
  projectId: number | null = null;

  constructor(
    private fb: FormBuilder,
    private projectService: ProjectService,
    private route: ActivatedRoute,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.projectForm = this.fb.group({
      name: ['', Validators.required],
      description: ['', Validators.required],
      status: ['PLANNING', Validators.required],
      startDate: ['', Validators.required],
      endDate: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEditMode = true;
        this.projectId = +params['id'];
        this.loadProject(this.projectId);
      }
    });
  }

  loadProject(id: number): void {
    this.projectService.getProjectById(id).subscribe({
      next: (project) => {
        this.projectForm.patchValue({
          ...project,
          startDate: new Date(project.startDate),
          endDate: new Date(project.endDate)
        });
      },
      error: (error) => {
        this.snackBar.open('Error loading project', 'Close', { duration: 3000 });
        this.router.navigate(['/projects']);
      }
    });
  }

  onSubmit(): void {
    if (this.projectForm.valid) {
      const project: Project = this.projectForm.value;
      
      if (this.isEditMode && this.projectId) {
        this.projectService.updateProject(this.projectId, project).subscribe({
          next: () => {
            this.snackBar.open('Project updated successfully', 'Close', { duration: 3000 });
            this.router.navigate(['/projects']);
          },
          error: (error) => {
            this.snackBar.open('Error updating project', 'Close', { duration: 3000 });
          }
        });
      } else {
        this.projectService.createProject(project).subscribe({
          next: () => {
            this.snackBar.open('Project created successfully', 'Close', { duration: 3000 });
            this.router.navigate(['/projects']);
          },
          error: (error) => {
            this.snackBar.open('Error creating project', 'Close', { duration: 3000 });
          }
        });
      }
    }
  }

  goBack(): void {
    this.router.navigate(['/projects']);
  }
} 