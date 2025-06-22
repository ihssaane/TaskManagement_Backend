import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { CommentService } from '../../../services/comment.service';
import { Comment } from '../../../models/comment.model';

@Component({
  selector: 'app-comment-list',
  template: `
    <div class="comment-list-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>Comments</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <div class="filter-section">
            <mat-form-field appearance="outline">
              <mat-label>Filter Comments</mat-label>
              <input matInput [(ngModel)]="filterValue" (keyup)="applyFilter()" placeholder="Search by content...">
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>Sort By</mat-label>
              <mat-select [(ngModel)]="sortField" (selectionChange)="applySort()">
                <mat-option value="createdAt">Date</mat-option>
                <mat-option value="content">Content</mat-option>
              </mat-select>
            </mat-form-field>
          </div>

          <div class="comments-list">
            <mat-card *ngFor="let comment of filteredComments" class="comment-card">
              <mat-card-content>
                <div class="comment-header">
                  <span class="comment-author">{{ comment.author.username }}</span>
                  <span class="comment-date">{{ comment.createdAt | date:'medium' }}</span>
                </div>
                <p class="comment-content">{{ comment.content }}</p>
                <div class="comment-footer">
                  <span class="comment-task">Task: {{ comment.task.title }}</span>
                  <div class="comment-actions">
                    <button mat-icon-button color="primary" (click)="editComment(comment)">
                      <mat-icon>edit</mat-icon>
                    </button>
                    <button mat-icon-button color="warn" (click)="deleteComment(comment)">
                      <mat-icon>delete</mat-icon>
                    </button>
                  </div>
                </div>
              </mat-card-content>
            </mat-card>
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .comment-list-container {
      padding: 20px;
    }
    .filter-section {
      display: flex;
      gap: 20px;
      margin-bottom: 20px;
    }
    .comments-list {
      display: flex;
      flex-direction: column;
      gap: 15px;
    }
    .comment-card {
      margin-bottom: 10px;
    }
    .comment-header {
      display: flex;
      justify-content: space-between;
      margin-bottom: 10px;
    }
    .comment-author {
      font-weight: bold;
    }
    .comment-date {
      color: #666;
      font-size: 0.9em;
    }
    .comment-content {
      margin: 10px 0;
      white-space: pre-wrap;
    }
    .comment-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 10px;
      padding-top: 10px;
      border-top: 1px solid #eee;
    }
    .comment-task {
      color: #666;
      font-size: 0.9em;
    }
    .comment-actions {
      display: flex;
      gap: 5px;
    }
  `]
})
export class CommentListComponent implements OnInit {
  comments: Comment[] = [];
  filteredComments: Comment[] = [];
  filterValue: string = '';
  sortField: string = 'createdAt';

  constructor(
    private commentService: CommentService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadComments();
  }

  loadComments(): void {
    this.commentService.getAllComments().subscribe({
      next: (comments) => {
        this.comments = comments;
        this.applyFilter();
      },
      error: (error) => {
        this.snackBar.open('Error loading comments', 'Close', { duration: 3000 });
      }
    });
  }

  applyFilter(): void {
    this.filteredComments = this.comments.filter(comment =>
      comment.content.toLowerCase().includes(this.filterValue.toLowerCase())
    );
    this.applySort();
  }

  applySort(): void {
    this.filteredComments.sort((a, b) => {
      if (this.sortField === 'createdAt') {
        return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
      } else {
        return a.content.localeCompare(b.content);
      }
    });
  }

  editComment(comment: Comment): void {
    this.router.navigate(['/comments', comment.id, 'edit']);
  }

  deleteComment(comment: Comment): void {
    if (confirm('Are you sure you want to delete this comment?')) {
      this.commentService.deleteComment(comment.id).subscribe({
        next: () => {
          this.snackBar.open('Comment deleted successfully', 'Close', { duration: 3000 });
          this.loadComments();
        },
        error: (error) => {
          this.snackBar.open('Error deleting comment', 'Close', { duration: 3000 });
        }
      });
    }
  }
} 