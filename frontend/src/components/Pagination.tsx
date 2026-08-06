import React from 'react';

interface PaginationProps {
  pageNumber: number;
  totalPages: number;
  totalElements: number;
  pageSize: number;
  onPageChange: (newPage: number) => void;
  onPageSizeChange?: (newSize: number) => void;
}

export const Pagination: React.FC<PaginationProps> = ({
  pageNumber,
  totalPages,
  totalElements,
  pageSize,
  onPageChange,
  onPageSizeChange,
}) => {
  if (totalPages <= 1) return null;

  return (
    <div className="flex items-center justify-between px-4 py-3 bg-white border-t border-gray-200 sm:px-6">
      <div className="flex justify-between items-center w-full">
        <p className="text-sm text-gray-700">
          Showing <span className="font-medium">{pageNumber * pageSize + 1}</span> to{' '}
          <span className="font-medium">
            {Math.min((pageNumber + 1) * pageSize, totalElements)}
          </span>{' '}
          of <span className="font-medium">{totalElements}</span> results
        </p>

        <div className="inline-flex space-x-2">
          <button
            onClick={() => onPageChange(pageNumber - 1)}
            disabled={pageNumber === 0}
            className="px-3 py-1 text-sm bg-gray-100 hover:bg-gray-200 text-gray-800 rounded disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Previous
          </button>

          <span className="text-sm text-gray-600 self-center px-2">
            Page {pageNumber + 1} of {totalPages}
          </span>

          <button
            onClick={() => onPageChange(pageNumber + 1)}
            disabled={pageNumber >= totalPages - 1}
            className="px-3 py-1 text-sm bg-gray-100 hover:bg-gray-200 text-gray-800 rounded disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Next
          </button>
        </div>
      </div>
    </div>
  );
};