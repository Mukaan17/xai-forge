export interface ProblemDetail {
  type: string;
  title?: string;
  status: number;
  detail: string;
  instance?: string;
  errorCode?: string;
  correlationId?: string;
  timestamp?: string;
  path?: string;
  errors?: ValidationError[];
  details?: Record<string, unknown>;
}

export interface ValidationError {
  field: string;
  message: string;
  rejectedValue?: unknown;
}

export interface PaginatedResponse<T> {
  content: T[];
  currentPage: number;
  totalItems: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

export interface PaginationParams {
  page?: number;
  size?: number;
  search?: string;
  dateFrom?: string;
  dateTo?: string;
}

export class ApiError extends Error {
  constructor(
    public readonly status: number,
    public readonly errorCode: string,
    public readonly detail: string,
    public readonly correlationId?: string,
    public readonly validationErrors?: ValidationError[]
  ) {
    super(detail);
    this.name = 'ApiError';
  }

  static fromProblemDetail(problem: ProblemDetail): ApiError {
    return new ApiError(
      problem.status,
      problem.errorCode ?? 'UNKNOWN',
      problem.detail,
      problem.correlationId,
      problem.errors
    );
  }

  get isValidationError(): boolean {
    return this.status === 400 && !!this.validationErrors?.length;
  }

  get isAuthError(): boolean {
    return this.status === 401 || this.status === 403;
  }

  get isNotFound(): boolean {
    return this.status === 404;
  }

  get isServerError(): boolean {
    return this.status >= 500;
  }

  get isRateLimited(): boolean {
    return this.status === 429;
  }
}
