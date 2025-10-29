import { HttpInterceptorFn } from '@angular/common/http';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('jwt'); // or from a service

  console.log('✅ JWT Interceptor triggered for:', req.url);

  if (token) {
    console.log('✅ Token attached:', token.substring(0, 15) + '...');
    const cloned = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
    return next(cloned);
  } else {
    console.warn('⚠️ No token found, sending request without Authorization header');
  }

  return next(req);
};
