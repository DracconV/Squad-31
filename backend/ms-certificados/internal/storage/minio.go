package storage

import (
	"bytes"
	"context"
	"fmt"
	"os"

	"github.com/minio/minio-go/v7"
	"github.com/minio/minio-go/v7/pkg/credentials"
)

type MinioStorage struct {
	client *minio.Client
	bucket string
}

func NewMinioStorage() (*MinioStorage, error) {
	endpoint := envDefault("MINIO_ENDPOINT", "localhost:9000")
	accessKey := envDefault("MINIO_ACCESS_KEY", "seed_minio_user")
	secretKey := envDefault("MINIO_SECRET_KEY", "seed_minio_pass")
	bucket := envDefault("MINIO_BUCKET", "certificados")

	client, err := minio.New(endpoint, &minio.Options{
		Creds:  credentials.NewStaticV4(accessKey, secretKey, ""),
		Secure: false,
	})
	if err != nil {
		return nil, fmt.Errorf("minio: %w", err)
	}

	ctx := context.Background()
	exists, err := client.BucketExists(ctx, bucket)
	if err != nil {
		return nil, fmt.Errorf("minio: verificar bucket: %w", err)
	}
	if !exists {
		if err := client.MakeBucket(ctx, bucket, minio.MakeBucketOptions{}); err != nil {
			return nil, fmt.Errorf("minio: criar bucket: %w", err)
		}
	}

	return &MinioStorage{client: client, bucket: bucket}, nil
}

func (s *MinioStorage) UploadPDF(ctx context.Context, nome string, data []byte) (string, error) {
	_, err := s.client.PutObject(
		ctx, s.bucket, nome,
		bytes.NewReader(data), int64(len(data)),
		minio.PutObjectOptions{ContentType: "application/pdf"},
	)
	if err != nil {
		return "", fmt.Errorf("minio upload: %w", err)
	}
	return fmt.Sprintf("/certificados/%s", nome), nil
}

func envDefault(key, fallback string) string {
	if v := os.Getenv(key); v != "" {
		return v
	}
	return fallback
}
