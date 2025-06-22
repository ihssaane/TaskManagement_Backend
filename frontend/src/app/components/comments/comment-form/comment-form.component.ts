import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CommentService } from '../../../services/comment.service';
import { TaskService } from '../../../services/task.service';
import { Comment } from '../../../models/comment.model';
import { Task } from '../../../models/task.model';

@Component({
  selector: 'app-comment-form',
  template: `
    <div class="comment-form-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>{{ isEditMode ? 'Edit Comment' : 'Add New Comment' }}</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <form [formGroup]="commentForm" (ngSubmit)="onSubmit()">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Task</mat-label>
              <mat-select formControlName="taskId" required [disabled]="isEditMode">
                <mat-option *ngFor="let task of tasks" [value]="task.id">
                  {{ task.title }}
                </mat-option>
              </mat-select>
              <mat-error *ngIf="commentForm.get('taskId')?.hasError('required')">
                Task is required
              </mat-error>
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Comment</mat-label>
              <textarea matInput formControlName="content" rows="4" required></textarea>
              <mat-error *ngIf="commentForm.get('content')?.hasError('required')">
                Comment content is required
              </mat-error>
            </mat-form-field>

            <div class="form-actions">
              <button mat-button type="button" (click)="goBack()">Cancel</button>
              <button mat-raised-button color="primary" type="submit" [disabled]="commentForm.invalid">
                {{ isEditMode ? 'Update' : 'Add' }}
              </button>
            </div>
          </form>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .comment-form-container {
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
export class CommentFormComponent implements OnInit {
  commentForm: FormGroup;
  isEditMode = false;
  commentId: number | null = null;
  tasks: Task[] = [];

  constructor(
    private fb: FormBuilder,
    private commentService: CommentService,
    private taskService: TaskService,
    private route: ActivatedRoute,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.commentForm = this.fb.group({
      taskId: ['', Validators.required],
      content: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadTasks();
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEditMode = true;
        this.commentId = +params['id'];
        this.loadComment(this.commentId);
      }
    });
  }

  loadTasks(): void {
    this.taskService.getAllTasks().subscribe({
      next: (tasks) => {
        this.tasks = tasks;
      },
      error: (error) => {
        this.snackBar.open('Error loading tasks', 'Close', { duration: 3000 });
      }
    });
  }

  loadComment(id: number): void {
    this.commentService.getCommentById(id).subscribe({
      next: (comment) => {
        this.commentForm.patchValue({
          taskId: comment.task.id,
          content: comment.content
        });
      },
      error: (error) => {
        this.snackBar.open('Error loading comment', 'Close', { duration: 3000 });
        this.router.navigate(['/comments']);
      }
    });
  }

  onSubmit(): void {
    if (this.commentForm.valid) {
      const comment: Comment = {
        ...this.commentForm.value,
        task: this.tasks.find(t => t.id === this.commentForm.value.taskId)
      };
      
      if (this.isEditMode && this.commentId) {
        this.commentService.updateComment(this.commentId, comment).subscribe({
          next: () => {
            this.snackBar.open('Comment updated successfully', 'Close', { duration: 3000 });
            this.router.navigate(['/comments']);
          },
          error: (error) => {
            this.snackBar.open('Error updating comment', 'Close', { duration: 3000 });
          }
        });
      } else {
        this.commentService.createComment(comment).subscribe({
          next: () => {
            this.snackBar.open('Comment created successfully', 'Close', { duration: 3000 });
            this.router.navigate(['/comments']);
          },
          error: (error) => {
            this.snackBar.open('Error creating comment', 'Close', { duration: 3000 });
          }
        });
      }
    }
  }

  goBack(): void {
    this.router.navigate(['/comments']);
  }
} 