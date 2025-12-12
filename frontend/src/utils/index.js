/**
 * Utility functions
 */
import { format, formatDistanceToNow, parseISO } from 'date-fns';
import clsx from 'clsx';
import { twMerge } from 'tailwind-merge';

/**
 * Tailwind class merge utility
 */
export function cn(...inputs) {
  return twMerge(clsx(inputs));
}

/**
 * Format date (short format)
 */
export function formatDate(date, formatStr = 'MMM dd, yyyy') {
  if (!date) return '';
  try {
    const dateObj = typeof date === 'string' ? parseISO(date) : date;
    return format(dateObj, formatStr);
  } catch (error) {
    return '';
  }
}

/**
 * Format date (long format)
 */
export function formatDateLong(date) {
  return formatDate(date, 'MMMM dd, yyyy');
}

/**
 * Format relative time (e.g., "2 hours ago")
 */
export function formatRelativeTime(date) {
  if (!date) return '';
  try {
    const dateObj = typeof date === 'string' ? parseISO(date) : date;
    return formatDistanceToNow(dateObj, { addSuffix: true });
  } catch (error) {
    return '';
  }
}

/**
 * Format number with commas
 */
export function formatNumber(num) {
  if (num === null || num === undefined) return '0';
  return new Intl.NumberFormat('en-US').format(num);
}

/**
 * Format percentage
 */
export function formatPercentage(value, decimals = 2) {
  if (value === null || value === undefined) return '0%';
  return `${(value * 100).toFixed(decimals)}%`;
}

/**
 * Format file size
 */
export function formatFileSize(bytes) {
  if (!bytes || bytes === 0) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return `${parseFloat((bytes / Math.pow(k, i)).toFixed(2))} ${sizes[i]}`;
}

/**
 * Format status string (capitalize, replace underscores)
 */
export function formatStatus(status) {
  if (!status) return '';
  return status
    .toString()
    .toLowerCase()
    .replace(/_/g, ' ')
    .replace(/\b\w/g, (l) => l.toUpperCase());
}

/**
 * Get status color class
 */
export function getStatusColor(status) {
  const statusColors = {
    ready: 'text-green-600',
    training: 'text-blue-600',
    failed: 'text-red-600',
    pending: 'text-yellow-600',
    uploading: 'text-blue-600',
    processing: 'text-blue-600',
    completed: 'text-green-600',
    active: 'text-green-600',
    inactive: 'text-gray-600',
    archived: 'text-gray-500',
  };
  return statusColors[status?.toLowerCase()] || 'text-gray-600';
}

/**
 * Get status badge class
 */
export function getStatusBadgeClass(status) {
  const badgeClasses = {
    ready: 'bg-green-100 text-green-800',
    training: 'bg-blue-100 text-blue-800',
    failed: 'bg-red-100 text-red-800',
    pending: 'bg-yellow-100 text-yellow-800',
    uploading: 'bg-blue-100 text-blue-800',
    processing: 'bg-blue-100 text-blue-800',
    completed: 'bg-green-100 text-green-800',
    active: 'bg-green-100 text-green-800',
    inactive: 'bg-gray-100 text-gray-800',
    archived: 'bg-gray-100 text-gray-800',
  };
  return badgeClasses[status?.toLowerCase()] || 'bg-gray-100 text-gray-800';
}

/**
 * Format algorithm name
 */
export function formatAlgorithm(algorithm) {
  if (!algorithm) return '';
  return algorithm
    .toString()
    .replace(/_/g, ' ')
    .replace(/\b\w/g, (l) => l.toUpperCase());
}

/**
 * Validate email
 */
export function isValidEmail(email) {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return emailRegex.test(email);
}

/**
 * Validate password with error messages
 */
export function isValidPassword(password) {
  if (!password) {
    return { valid: false, error: 'Password is required' };
  }
  if (password.length < 8) {
    return { valid: false, error: 'Password must be at least 8 characters' };
  }
  if (!/[A-Z]/.test(password)) {
    return { valid: false, error: 'Password must contain at least one uppercase letter' };
  }
  if (!/[a-z]/.test(password)) {
    return { valid: false, error: 'Password must contain at least one lowercase letter' };
  }
  if (!/[0-9]/.test(password)) {
    return { valid: false, error: 'Password must contain at least one number' };
  }
  return { valid: true, error: null };
}

/**
 * Mask IP address (show only first 3 octets)
 */
export function maskIpAddress(ip) {
  if (!ip) return '';
  const parts = ip.split('.');
  if (parts.length === 4) {
    return `${parts[0]}.${parts[1]}.${parts[2]}.xxx`;
  }
  return ip;
}

/**
 * Truncate text with ellipsis
 */
export function truncate(text, maxLength = 50) {
  if (!text) return '';
  if (text.length <= maxLength) return text;
  return text.substring(0, maxLength) + '...';
}
